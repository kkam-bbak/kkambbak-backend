package com.kkambbak.domain.payment.service;

import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.repository.payment.SubscriptionPlanRepository;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import com.kkambbak.domain.payment.exception.PlanNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * 구독 생성
     */
    public Subscription createSubscription(Long userId, Long planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
            .orElseThrow(PlanNotFoundException::new);

        subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .ifPresent(existing -> {
                existing.expire();
                subscriptionRepository.save(existing);
                log.info("Previous subscription expired - userId: {}, subscriptionId: {}", userId, existing.getId());
            });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(plan.getDurationDays());

        Subscription subscription = Subscription.builder()
            .userId(userId)
            .plan(plan)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(now)
            .endDate(endDate)
            .autoRenew(false)
            .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Premium 플랜 조회
     */
    @Transactional(readOnly = true)
    public SubscriptionPlan getPremiumPlan() {
        return subscriptionPlanRepository.findByName("Premium")
            .orElseThrow(PlanNotFoundException::new);
    }
}