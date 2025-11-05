package com.kkambbak.domain.payment.service;

import com.kkambbak.client.payment.service.KakaoPayService;
import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.entity.payment.enums.PaymentMethod;
import com.kkambbak.core.entity.payment.enums.PaymentStatus;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.core.repository.payment.SubscriptionPlanRepository;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.payment.dto.PaymentDetailDto;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.dto.PaymentResultDto;
import com.kkambbak.domain.payment.exception.*;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final KakaoPayService kakaoPayService;
    private final PayHistoryRepository payHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    public PaymentDto.CreateResponse createPayment(Long userId, Long planId, boolean autoRenew) {
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

            Map<String, Object> readyResponse;
            if (autoRenew) {
                readyResponse = kakaoPayService.readySubscription(
                    orderId,
                    userId.toString(),
                    plan.getName(),
                    1,
                    plan.getPrice().intValue(),
                    0
                );
            } else {
                readyResponse = kakaoPayService.readyPayment(
                    orderId,
                    userId.toString(),
                    plan.getName(),
                    1,
                    plan.getPrice().intValue(),
                    0
                );
            }

            String tid = (String) readyResponse.get("tid");
            String approvalUrl = (String) readyResponse.get("next_redirect_pc_url");

            Map<String, Object> paymentData = savedPayHistory.getPaymentData();
            paymentData.put("tid", tid);
            paymentData.put("orderId", orderId);
            paymentData.put("autoRenew", autoRenew);
            payHistoryRepository.save(savedPayHistory);

            return PaymentDto.CreateResponse.builder()
                .paymentId(savedPayHistory.getId())
                .orderId(orderId)
                .approvalUrl(approvalUrl)
                .build();

        } catch (IOException e) {
            log.error("Failed to create payment - userId: {}, planId: {}, autoRenew: {}", userId, planId, autoRenew, e);
            throw new PaymentCreationFailedException(e.getMessage());
        }
    }

    public void approvePayment(Long userId, Long paymentId, String orderId, String pgToken, boolean autoRenew) {
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

            Map<String, Object> approveResponse = kakaoPayService.approvePayment(tid, orderId, userId.toString(), pgToken, autoRenew);

            paymentData.put("kakaoPayTid", tid);
            paymentData.put("kakaoPayOrderId", orderId);
            paymentData.put("kakaoPayStatus", approveResponse.get("status"));

            if (autoRenew) {
                String sid = (String) approveResponse.get("sid");
                paymentData.put("sid", sid);
            }

            payHistory.complete(orderId, paymentData, null);
            payHistoryRepository.save(payHistory);

        } catch (IOException e) {
            log.error("Failed to approve payment - userId: {}, paymentId: {}, orderId: {}, autoRenew: {}", userId, paymentId, orderId, autoRenew, e);
            throw new PaymentCaptureFailedException(e.getMessage());
        }
    }

    public String getApprovalSuccessUrl(String orderId) {
        return kakaoPayService.getSuccessUrl() + "?orderId=" + orderId;
    }

    public String getApprovalFailUrl() {
        return kakaoPayService.getFailUrl();
    }

    @Transactional(readOnly = true)
    public PaymentDetailDto getPaymentDetail(Long userId, Long paymentId) {
        PayHistory payHistory = payHistoryRepository.findById(paymentId)
            .orElseThrow(PaymentNotFoundException::new);

        if (!payHistory.getUserId().equals(userId)) {
            throw new UnauthorizedPaymentAccessException();
        }

        return buildPaymentDetail(payHistory);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDetailDto> getPaymentList(Long userId, Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidPageRequestException("Page number must be >= 0");
        }

        Page<PayHistory> results = payHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<PaymentDetailDto> dtos = results.getContent().stream()
            .map(this::buildPaymentDetail)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PaymentResultDto getPaymentResult(Long userId) {
        Subscription activeSubscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElseThrow(SubscriptionNotFoundException::new);

        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        String planName = activeSubscription.getPlan().getName();

        return PaymentResultDto.builder()
            .userName(user.getName())
            .userEmail(user.getEmail())
            .planName(planName)
            .build();
    }

    private PaymentDetailDto buildPaymentDetail(PayHistory payHistory) {
        User user = userRepository.findById(payHistory.getUserId())
            .orElseThrow(UserNotFoundException::new);

        String planName = "N/A";
        LocalDateTime subscriptionStartDate = null;
        LocalDateTime subscriptionEndDate = null;

        if (payHistory.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(payHistory.getSubscriptionId())
                .orElse(null);
            if (subscription != null) {
                planName = subscription.getPlan().getName();
                subscriptionStartDate = subscription.getStartDate();
                subscriptionEndDate = subscription.getExpiredAt() != null ? subscription.getExpiredAt() : subscription.getEndDate();
            }
        }

        return PaymentDetailDto.builder()
            .paymentId(payHistory.getId())
            .userName(user.getName())
            .userEmail(user.getEmail())
            .planName(planName)
            .amount(payHistory.getAmount())
            .status(payHistory.getStatus().toString())
            .createdAt(payHistory.getCreatedAt())
            .paidAt(payHistory.getPaidAt())
            .subscriptionStartDate(subscriptionStartDate)
            .subscriptionEndDate(subscriptionEndDate)
            .build();
    }

}