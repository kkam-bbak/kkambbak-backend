package com.kkambbak.domain.payment.service;

import com.kkambbak.client.payment.service.KakaoPayService;
import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.entity.payment.enums.PaymentMethod;
import com.kkambbak.core.entity.payment.enums.PaymentStatus;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.core.repository.payment.SubscriptionPlanRepository;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final KakaoPayService kakaoPayService;
    private final PayHistoryRepository payHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    public PaymentDto.CreateResponse createPayment(Long userId, Long planId) {
        try {
            var pendingPayment = payHistoryRepository.findByUserIdAndStatusWithLock(userId, PaymentStatus.PENDING);
            if (pendingPayment.isPresent()) {
                throw new PaymentPendingException("진행 중인 결제가 있습니다. 진행 중인 결제를 완료해주세요.");
            }

            var activeSubscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
            if (activeSubscription.isPresent()) {
                Subscription subscription = activeSubscription.get();
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(),
                    subscription.getEndDate()
                );
                String message = String.format("이미 구독 중입니다. 남은 날짜: %d일", daysRemaining);
                throw new AlreadySubscribedException(message);
            }

            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(PlanNotFoundException::new);

            PayHistory payHistory = PayHistory.builder()
                .userId(userId)
                .paymentMethod(PaymentMethod.KAKAO)
                .amount(plan.getPrice())
                .status(PaymentStatus.PENDING)
                .paymentData(new HashMap<>())
                .build();
            PayHistory savedPayHistory = payHistoryRepository.save(payHistory);

            String orderId = "order_" + savedPayHistory.getId() + "_" + System.currentTimeMillis();
            Map<String, Object> readyResponse = kakaoPayService.readyPayment(
                orderId,
                userId.toString(),
                plan.getName(),
                1,
                plan.getPrice().intValue(),
                0
            );

            String tid = (String) readyResponse.get("tid");
            String approvalUrl = (String) readyResponse.get("next_redirect_pc_url");

            Map<String, Object> paymentData = savedPayHistory.getPaymentData();
            paymentData.put("tid", tid);
            paymentData.put("orderId", orderId);
            payHistoryRepository.save(savedPayHistory);

            return PaymentDto.CreateResponse.builder()
                .paymentId(savedPayHistory.getId())
                .orderId(orderId)
                .approvalUrl(approvalUrl)
                .build();

        } catch (IOException e) {
            log.error("Failed to create payment - userId: {}, planId: {}", userId, planId, e);
            throw new PaymentCreationFailedException(e.getMessage());
        }
    }
    
    public void capturePayment(Long userId, Long paymentId, String orderId, String pgToken) {
        PayHistory payHistory = payHistoryRepository.findByIdWithLock(paymentId)
            .orElseThrow(PaymentNotFoundException::new);

        if (!payHistory.getUserId().equals(userId)) {
            throw new UnauthorizedPaymentAccessException();
        }

        if (payHistory.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException("승인 가능한 결제가 아닙니다. 현재 상태: " + payHistory.getStatus());
        }

        try {
            Map<String, Object> paymentData = payHistory.getPaymentData();
            String tid = (String) paymentData.get("tid");

            if (tid == null || tid.isBlank()) {
                throw new InvalidPaymentStatusException("결제 데이터가 올바르지 않습니다.");
            }

            Map<String, Object> approveResponse = kakaoPayService.approvePayment(tid, orderId, userId.toString(), pgToken);

            Map<String, Object> updatePaymentData = new HashMap<>();
            updatePaymentData.put("kakaoPayTid", tid);
            updatePaymentData.put("kakaoPayOrderId", orderId);
            updatePaymentData.put("kakaoPayStatus", approveResponse.get("status"));

            // Facade에서 subscription ID를 전달받을 때까지는 null로 설정
            payHistory.complete(orderId, updatePaymentData, null);
            payHistoryRepository.save(payHistory);

        } catch (IOException e) {
            log.error("Failed to capture payment - userId: {}, paymentId: {}, orderId: {}", userId, paymentId, orderId, e);
            throw new PaymentCaptureFailedException(e.getMessage());
        }
    }

}