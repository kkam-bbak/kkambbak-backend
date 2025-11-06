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

    @Scheduled(cron = "${cron.subscription-job:0 0 12 * * *}")
    public void executeSubscriptionRenewal() {
        long startTime = System.currentTimeMillis();
        log.info("[SubscriptionJob] Starting subscription renewal batch job at {}",
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
            log.error("[SubscriptionJob] Subscription renewal batch job failed with error: {}",
                    e.getMessage(), e);
            discordClient.sendSchedulerError("SubscriptionRenewalJob", e);
        }
    }
}