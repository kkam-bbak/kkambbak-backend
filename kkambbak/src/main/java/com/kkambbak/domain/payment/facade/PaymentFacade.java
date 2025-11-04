package com.kkambbak.domain.payment.facade;

import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.domain.payment.dto.PaymentDto;
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

    public void approvePayment(Long userId, Long paymentId, String orderId, String pgToken) {
        PayHistory payHistory = payHistoryRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        boolean autoRenew = Boolean.TRUE.equals(payHistory.getPaymentData().get("autoRenew"));
        log.info("Approving payment - userId: {}, paymentId: {}, autoRenew: {}", userId, paymentId, autoRenew);

        paymentService.approvePayment(userId, paymentId, orderId, pgToken, autoRenew);

        SubscriptionPlan premiumPlan = subscriptionService.getPremiumPlan();
        Subscription subscription = subscriptionService.createSubscription(userId, premiumPlan.getId());

        PayHistory updatedPayHistory = payHistoryRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (autoRenew) {
            String billingKey = (String) updatedPayHistory.getPaymentData().get("sid");
            subscription.setBillingKey(billingKey);
            subscription.setAutoRenew(true);
        }

        updatedPayHistory.setSubscriptionId(subscription.getId());
        payHistoryRepository.save(updatedPayHistory);
    }
}