package com.kkambbak.scheduler.domain.payment.job;

import com.kkambbak.scheduler.domain.payment.service.SubscriptionExpiryReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryReminderJob {

    private final SubscriptionExpiryReminderService subscriptionExpiryReminderService;

    @Scheduled(cron = "${cron.subscription-expiry-reminder-job:0 0 10 * * *}")
    public void sendSubscriptionExpiryReminder() {
        log.info("[SubscriptionExpiryReminderJob] Starting subscription expiry reminder job");
        subscriptionExpiryReminderService.sendSubscriptionExpiryReminder();
    }
}