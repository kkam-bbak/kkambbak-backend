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

    public PaymentDto.CreateResponse createPayment(Long userId, Long planId) {
        log.info("Creating payment - userId: {}, planId: {}", userId, planId);
        return paymentService.createPayment(userId, planId);
    }

    public void capturePayment(Long userId, Long paymentId, String orderId, String pgToken) {
        paymentService.capturePayment(userId, paymentId, orderId, pgToken);

        SubscriptionPlan premiumPlan = subscriptionService.getPremiumPlan();
        Subscription subscription = subscriptionService.createSubscription(userId, premiumPlan.getId());

        PayHistory payHistory = payHistoryRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payHistory.setSubscriptionId(subscription.getId());
        payHistoryRepository.save(payHistory);
    }
}