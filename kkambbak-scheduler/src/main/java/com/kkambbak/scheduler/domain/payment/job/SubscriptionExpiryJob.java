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
public class SubscriptionExpiryJob {

    private final SubscriptionBatchService subscriptionBatchService;
    private final DiscordClient discordClient;

    @Scheduled(cron = "${cron.subscription-expiry-job:0 0 0 * * *}")
    public void executeSubscriptionExpiry() {
        long startTime = System.currentTimeMillis();

        try {
            List<Subscription> targetSubscriptions = subscriptionBatchService.getExpiredTargets();

            if (targetSubscriptions.isEmpty()) {
                double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                discordClient.sendSchedulerSuccess("SubscriptionExpiryJob", executionTimeSeconds);
                return;
            }

            log.info("[SubscriptionExpiryJob] Found {} expired subscriptions to process", targetSubscriptions.size());
            subscriptionBatchService.processAllExpiries(targetSubscriptions);
            double executionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            discordClient.sendSchedulerSuccess("SubscriptionExpiryJob", executionTimeSeconds);

        } catch (Exception e) {
            log.error("[SubscriptionExpiryJob] Subscription expiry batch job failed with error: {}",
                    e.getMessage(), e);
            discordClient.sendSchedulerError("SubscriptionExpiryJob", e);
        }
    }
}