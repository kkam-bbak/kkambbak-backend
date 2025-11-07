package com.kkambbak.scheduler.domain.payment.job;

import com.kkambbak.client.discord.DiscordClient;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.scheduler.domain.payment.service.SubscriptionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionJob {

    private final SubscriptionBatchService subscriptionRenewalBatchService;
    private final DiscordClient discordClient;

    /**
     * 정기 결제 갱신 (전날 12시 실행)
     */
    @Scheduled(cron = "${cron.subscription-job:0 0 12 * * *}")
    public void executeSubscriptionRenewal() {
        long startTime = System.currentTimeMillis();
        log.info("[SubscriptionRenewalJob] Starting subscription renewal batch job at {}",
                LocalDateTime.now());

        try {
            List<Subscription> targetSubscriptions = subscriptionRenewalBatchService.getExpireTargets();

            if (targetSubscriptions.isEmpty()) {
                double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                discordClient.sendSchedulerSuccess("SubscriptionRenewalJob", executionTimeSeconds);
                return;
            }

            subscriptionRenewalBatchService.processAllRenewals(targetSubscriptions);
            double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            discordClient.sendSchedulerSuccess("SubscriptionRenewalJob", executionTimeSeconds);

        } catch (Exception e) {
            log.error("[SubscriptionRenewalJob] Subscription renewal batch job failed with error: {}",
                    e.getMessage(), e);
            discordClient.sendSchedulerError("SubscriptionRenewalJob", e);
        }
    }

    /**
     * 정기 결제 재시도 실패 한것들만 (당일 정오 실행)
     */
    @Scheduled(cron = "${cron.subscription-job:0 0 12 * * *}")
    public void executeSubscriptionRetry() {
        long startTime = System.currentTimeMillis();

        try {
            List<Subscription> retryTargets = subscriptionRenewalBatchService.getRetryTargets();

            if (retryTargets.isEmpty()) {
                double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                discordClient.sendSchedulerSuccess("SubscriptionRetryJob", executionTimeSeconds);
                return;
            }

            log.info("[SubscriptionRetryJob] Found {} subscriptions to retry (failed yesterday)", retryTargets.size());
            subscriptionRenewalBatchService.processAllRenewals(retryTargets);
            double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            discordClient.sendSchedulerSuccess("SubscriptionRetryJob", executionTimeSeconds);

        } catch (Exception e) {
            log.error("[SubscriptionRetryJob] Subscription retry job failed with error: {}",
                    e.getMessage(), e);
            discordClient.sendSchedulerError("SubscriptionRetryJob", e);
        }
    }
}