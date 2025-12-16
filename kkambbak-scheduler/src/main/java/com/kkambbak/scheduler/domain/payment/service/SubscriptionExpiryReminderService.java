package com.kkambbak.scheduler.domain.payment.service;

import com.kkambbak.client.discord.DiscordClient;
import com.kkambbak.client.mail.service.MailSender;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.payment.SubscriptionRepository;
import com.kkambbak.core.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionExpiryReminderService {

    private final SubscriptionRepository subscriptionRepository;
    private final MailSender mailSender;
    private final UserRoleService userRoleService;
    private final DiscordClient discordClient;

    @Transactional
    public void sendSubscriptionExpiryReminder() {
        long startTime = System.currentTimeMillis();

        try {
            LocalDateTime reminderDate = LocalDateTime.now().plusDays(2);
            List<Subscription> subscriptions = subscriptionRepository.findSubscriptionsExpiringOn(reminderDate, SubscriptionStatus.ACTIVE);

            if (subscriptions.isEmpty()) {
                log.info("[SubscriptionExpiryReminder] No subscriptions expiring in 2 days");
                long executionTime = System.currentTimeMillis() - startTime;
                double executionTimeSeconds = executionTime / 1000.0;
                discordClient.sendSchedulerSuccess("SubscriptionExpiryReminderJob", executionTimeSeconds);
                return;
            }

            log.info("[SubscriptionExpiryReminder] Found {} subscriptions expiring in 2 days", subscriptions.size());

            for (Subscription subscription : subscriptions) {
                try {
                    sendReminderEmail(subscription);
                } catch (Exception e) {
                    log.error("[SubscriptionExpiryReminder] Failed to send reminder for subscription {} - {}",
                        subscription.getId(), e.getMessage());
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            double executionTimeSeconds = executionTime / 1000.0;
            discordClient.sendSchedulerSuccess("SubscriptionExpiryReminderJob", executionTimeSeconds);

        } catch (Exception e) {
            log.error("[SubscriptionExpiryReminder] Job failed with error", e);
            discordClient.sendSchedulerError("SubscriptionExpiryReminderJob", e);
        }
    }

    private void sendReminderEmail(Subscription subscription) {
        try {
            User user = userRoleService.getUser(subscription.getUserId());
            mailSender.sendSubscriptionExpiryReminderEmail(
                user.getEmail(),
                user.getName(),
                subscription.getEndDate(),
                subscription.getPlan().getName()
            );
        } catch (Exception e) {
            log.error("[SubscriptionExpiryReminder] Failed to send email - subscriptionId: {}, error: {}",
                subscription.getId(), e.getMessage());
        }
    }
}