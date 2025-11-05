package com.kkambbak.domain.payment.facade;

import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.exception.PaymentNotFoundException;
import com.kkambbak.domain.payment.service.PaymentService;
import com.kkambbak.domain.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class PaymentFacade {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final PayHistoryRepository payHistoryRepository;

    public PaymentDto.CreateResponse createPayment(Long userId, Long planId, PaymentDto.CreateRequest request) {
        boolean autoRenew = request != null && Boolean.TRUE.equals(request.getAutoRenew());
        log.info("Creating payment - userId: {}, planId: {}, autoRenew: {}", userId, planId, autoRenew);

        return paymentService.createPayment(userId, planId, autoRenew);
    }

    public String approvePayment(String orderId, String pgToken) {
        try {
            PayHistory payHistory = payHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));

            Long userId = payHistory.getUserId();
            Long paymentId = payHistory.getId();
            boolean autoRenew = Boolean.TRUE.equals(payHistory.getPaymentData().get("autoRenew"));

            paymentService.approvePayment(userId, paymentId, orderId, pgToken, autoRenew);

            SubscriptionPlan premiumPlan = subscriptionService.getPremiumPlan();
            Subscription subscription = subscriptionService.createSubscription(userId, premiumPlan.getId());

            if (autoRenew) {
                String billingKey = (String) payHistory.getPaymentData().get("sid");
                subscription.setBillingKey(billingKey);
                subscription.setAutoRenew(true);
            }

            payHistory.setSubscriptionId(subscription.getId());
            payHistoryRepository.save(payHistory);

            return paymentService.getApprovalSuccessUrl(orderId);

        } catch (Exception e) {
            log.error("Payment approval failed - orderId: {}, error: {}", orderId, e.getMessage());
            return paymentService.getApprovalFailUrl();
        }
    }
}