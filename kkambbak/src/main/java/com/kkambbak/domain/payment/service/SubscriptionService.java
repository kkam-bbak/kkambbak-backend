package com.kkambbak.domain.payment.service;

import com.kkambbak.client.mail.service.MailSender;
import com.kkambbak.client.payment.service.KakaoPayService;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.payment.SubscriptionPlanRepository;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import com.kkambbak.core.service.UserRoleService;
import com.kkambbak.domain.payment.dto.SubscriptionDto;
import com.kkambbak.domain.payment.exception.PlanNotFoundException;
import com.kkambbak.domain.payment.exception.SubscriptionNotFoundException;
import com.kkambbak.domain.payment.exception.SubscriptionAlreadyCancelledException;
import com.kkambbak.domain.payment.exception.SubscriptionAlreadyExpiredException;
import com.kkambbak.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final KakaoPayService kakaoPayService;
    private final UserRoleService userRoleService;
    private final MailSender mailSender;

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

    /**
     * 사용자 구독 상태 조회 및 권한 검증
     * - ACTIVE 또는 CANCELLED 구독 조회 (CANCELLED도 ACTIVE로 반환)
     * - 만료됐으면 바로 EXPIRED로 변경 및 권한 다운그레이드
     * - 구독 상태 반환 (ACTIVE, EXPIRED, null)
     */
    public SubscriptionStatus verifySubscriptionStatus(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElseGet(() -> subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.CANCELLED)
                .orElse(null));

        if (subscription == null) {
            return null;
        }

        if (!subscription.isActive()) {
            subscription.expire();
            subscriptionRepository.save(subscription);
            userRoleService.downgradeRole(userId);

            return SubscriptionStatus.EXPIRED;
        }

        return SubscriptionStatus.ACTIVE;
    }

    public void cancelSubscription(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(SubscriptionNotFoundException::new);

        if (!subscription.getUserId().equals(userId)) {
            throw new SubscriptionNotFoundException();
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionAlreadyCancelledException();
        }

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new SubscriptionAlreadyExpiredException();
        }

        subscription.setAutoRenew(false);

        if (subscription.getBillingKey() != null && !subscription.getBillingKey().isEmpty()) {
            try {
                kakaoPayService.inactiveSubscription(subscription.getBillingKey());
            } catch (IOException e) {
                log.error("Failed to inactive subscription in KakaoPay - subscriptionId: {}, sid: {}, error: {}",
                    subscriptionId, subscription.getBillingKey(), e.getMessage());
            }
        }

        subscription.cancel();
        subscriptionRepository.save(subscription);

        try {
            User user = userRoleService.getUser(userId);
            mailSender.sendSubscriptionCancelledEmail(
                user.getEmail(),
                user.getName(),
                LocalDateTime.now(),
                subscription.getEndDate(),
                subscription.getPlan().getName()
            );
        } catch (Exception e) {
            log.warn("Failed to send subscription cancelled email", e);
        }
    }
    
    /**
     * 사용자의 활성화 구독 정보 조회
     * - ACTIVE 또는 CANCELLED 상태의 구독 반환
     * - 활성화 된 구독 상품이 없으면 예외처리
     * - 구독 취소 api등 사용할 때 구독 중인 상품 ID 알아내기 위함
     * - 구독 상태 검증 시 사용 할 메소드는 verifySubscriptionStatus 메소드 사용하세요.
     */
    @Transactional(readOnly = true)
    public SubscriptionDto getActiveSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElse(null);

        if (subscription == null) {
            subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.CANCELLED)
                .orElse(null);
        }

        if (subscription == null) {
            throw new SubscriptionNotFoundException("No active or cancelled subscription found for user: " + userId);
        }

        return SubscriptionDto.from(subscription);
    }
    
}