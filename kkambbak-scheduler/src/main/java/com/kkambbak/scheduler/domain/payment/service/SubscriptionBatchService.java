package com.kkambbak.scheduler.domain.payment.service;

import com.kkambbak.client.discord.DiscordClient;
import com.kkambbak.client.payment.service.KakaoPayService;
import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.enums.PaymentMethod;
import com.kkambbak.core.entity.payment.enums.PaymentStatus;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionBatchService {

    private final SubscriptionRepository subscriptionRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final KakaoPayService kakaoPayService;
    private final DiscordClient discordClient;

    /**
     * 내일 갱신 대상 조회 (전날 오후 12시에 처리하기 위해 내일 날짜로 조회)
     */
    public List<Subscription> getExpireTargets() {
        return subscriptionRepository.findDueForRenewal(
                LocalDateTime.now().plusDays(1),
                SubscriptionStatus.ACTIVE
        );
    }

    @Transactional
    public void processAllRenewals(List<Subscription> subscriptions) {
        for (Subscription subscription : subscriptions) {
            try {
                processSubscriptionRenewal(subscription);
            } catch (Exception e) {
                log.error("[SubscriptionRenewal] Failed to process subscription renewal - " +
                        "subscriptionId: {}, userId: {}, error: {}",
                        subscription.getId(), subscription.getUserId(), e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void processSubscriptionRenewal(Subscription subscription) {
        try {
            Map<String, Object> kakaoResponse = kakaoPayService.payWithSubscription(
                    subscription.getBillingKey(),
                    generateOrderId(subscription),
                    String.valueOf(subscription.getUserId()),
                    subscription.getPlan().getName(),
                    1,
                    subscription.getPlan().getPrice().intValue(),
                    0
            );

            handlePaymentSuccess(subscription, kakaoResponse);

        } catch (Exception e) {
            log.error("[SubscriptionRenewal] KakaoPay API call failed - subscriptionId: {}, error: {}",
                    subscription.getId(), e.getMessage());
            handlePaymentFailure(subscription, e);
        }
    }

    private void handlePaymentSuccess(Subscription subscription, Map<String, Object> kakaoResponse) {
        try {
            String tid = (String) kakaoResponse.get("tid");
            String aid = (String) kakaoResponse.get("aid");

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("sid", subscription.getBillingKey());
            paymentData.put("orderId", generateOrderId(subscription));
            paymentData.put("aid", aid);
            paymentData.put("tid", tid);
            paymentData.putAll(kakaoResponse);

            PayHistory payHistory = PayHistory.builder()
                    .userId(subscription.getUserId())
                    .subscriptionId(subscription.getId())
                    .paymentMethod(PaymentMethod.KAKAO)
                    .amount(subscription.getPlan().getPrice())
                    .status(PaymentStatus.COMPLETED)
                    .transactionId(tid)
                    .paymentData(paymentData)
                    .paidAt(LocalDateTime.now())
                    .build();

            payHistoryRepository.save(payHistory);

            LocalDateTime newEndDate = subscription.getEndDate().plusMonths(1);
            subscription.setEndDate(newEndDate);
            subscriptionRepository.save(subscription);
            try {
                discordClient.sendSubscriptionRenewalSuccess(
                        subscription.getUserId(),
                        subscription.getId(),
                        subscription.getPlan().getPrice(),
                        subscription.getPlan().getName(),
                        newEndDate
                );
            } catch (Exception discordError) {
                log.warn("[SubscriptionRenewal] Failed to send Discord success notification - subscriptionId: {}",
                        subscription.getId(), discordError);
            }

        } catch (Exception e) {
            log.error("[SubscriptionRenewal] Failed to handle payment success - subscriptionId: {}",
                    subscription.getId(), e);
            throw new RuntimeException("Payment success handling failed", e);
        }
    }

    private void handlePaymentFailure(Subscription subscription, Exception exception) {
        try {
            log.warn("[SubscriptionRenewal] Payment failure handling - subscriptionId: {}, error: {}",
                    subscription.getId(), exception.getMessage());
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("sid", subscription.getBillingKey());
            paymentData.put("orderId", generateOrderId(subscription));
            paymentData.put("errorMessage", exception.getMessage());
            paymentData.put("retryCount", 0);

            PayHistory payHistory = PayHistory.builder()
                    .userId(subscription.getUserId())
                    .subscriptionId(subscription.getId())
                    .paymentMethod(PaymentMethod.KAKAO)
                    .amount(subscription.getPlan().getPrice())
                    .status(PaymentStatus.FAILED)
                    .paymentData(paymentData)
                    .build();

            payHistoryRepository.save(payHistory);

            try {
                discordClient.sendSubscriptionRenewalFailure(
                        subscription.getUserId(),
                        subscription.getId(),
                        exception.getMessage(),
                        subscription.getPlan().getName()
                );
            } catch (Exception discordError) {
                log.warn("[SubscriptionRenewal] Failed to send Discord failure notification - subscriptionId: {}",
                        subscription.getId(), discordError);
            }

        } catch (Exception e) {
            log.error("[SubscriptionRenewal] Failed to handle payment failure - subscriptionId: {}",
                    subscription.getId(), e);
        }
    }

    private String generateOrderId(Subscription subscription) {
        return "renewal_" + subscription.getId() + "_" + System.currentTimeMillis();
    }
}