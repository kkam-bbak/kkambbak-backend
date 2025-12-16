package com.kkambbak.client.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DiscordClient {

    @Value("${discord.scheduler.webhook.url:}")
    private String schedulerWebhookUrl;

    @Value("${discord.payment.webhook.url:}")
    private String paymentWebhookUrl;

    private final RestTemplate restTemplate;

    public DiscordClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 스케줄러 에러를 디스코드로 전송
     */
    public void sendSchedulerError(String jobName, Exception exception) {
        if (schedulerWebhookUrl == null || schedulerWebhookUrl.isEmpty()) {
            log.warn("Discord scheduler webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildErrorMessage(jobName, exception);
            sendMessage(message, schedulerWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord webhook notification", e);
        }
    }

    /**
     * 스케줄러 성공 알림을 디스코드로 전송
     */
    public void sendSchedulerSuccess(String jobName, double executionTimeSeconds) {
        if (schedulerWebhookUrl == null || schedulerWebhookUrl.isEmpty()) {
            log.warn("Discord scheduler webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildSuccessMessage(jobName, executionTimeSeconds);
            sendMessage(message, schedulerWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord webhook notification", e);
        }
    }

    /**
     * 결제 성공 알림 (단건 결제)
     */
    public void sendPaymentSuccess(Long userId, Long paymentId, String paymentMethod,
                                   Long amount, String planName) {
        if (paymentWebhookUrl == null || paymentWebhookUrl.isEmpty()) {
            log.warn("Discord payment webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildPaymentSuccessMessage(userId, paymentId, paymentMethod, amount, planName);
            sendMessage(message, paymentWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord payment success notification", e);
        }
    }

    /**
     * 결제 실패 알림
     */
    public void sendPaymentFailure(Long userId, Long paymentId, String paymentMethod,
                                   String errorMessage, String planName) {
        if (paymentWebhookUrl == null || paymentWebhookUrl.isEmpty()) {
            log.warn("Discord payment webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildPaymentFailureMessage(userId, paymentId, paymentMethod, errorMessage, planName);
            sendMessage(message, paymentWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord payment failure notification", e);
        }
    }

    /**
     * 정기결제 성공 알림 (2회차+)
     */
    public void sendSubscriptionRenewalSuccess(Long userId, Long subscriptionId,
                                              Long amount, String planName,
                                              LocalDateTime newEndDate) {
        if (paymentWebhookUrl == null || paymentWebhookUrl.isEmpty()) {
            log.warn("Discord payment webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildSubscriptionRenewalSuccessMessage(userId, subscriptionId, amount, planName, newEndDate);
            sendMessage(message, paymentWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord subscription renewal success notification", e);
        }
    }

    /**
     * 정기결제 실패 알림 (2회차+)
     */
    public void sendSubscriptionRenewalFailure(Long userId, Long subscriptionId,
                                              String errorMessage, String planName) {
        if (paymentWebhookUrl == null || paymentWebhookUrl.isEmpty()) {
            log.warn("Discord payment webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildSubscriptionRenewalFailureMessage(userId, subscriptionId, errorMessage, planName);
            sendMessage(message, paymentWebhookUrl);
        } catch (Exception e) {
            log.error("Failed to send Discord subscription renewal failure notification", e);
        }
    }

    /**
     * 디스코드 메시지 전송
     */
    private void sendMessage(String content, String webhookUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(webhookUrl, request, String.class);
        log.info("Discord webhook notification sent successfully");
    }

    /**
     * 에러 메시지 포맷팅
     */
    private String buildErrorMessage(String jobName, Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 **스케줄러 에러 발생** 🚨\n");
        sb.append("```\n");
        sb.append("Job: ").append(jobName).append("\n");
        sb.append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Error: ").append(exception.getClass().getSimpleName()).append("\n");
        sb.append("Message: ").append(exception.getMessage()).append("\n");
        sb.append("```");
        return ensureMaxLength(sb.toString(), 2000);
    }

    /**
     * 성공 메시지 포맷팅
     */
    private String buildSuccessMessage(String jobName, double executionTimeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ **스케줄러 실행 완료** ✅\n");
        sb.append("```\n");
        sb.append("Job: ").append(jobName).append("\n");
        sb.append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Duration: ").append(String.format("%.2f", executionTimeSeconds)).append("초\n");
        sb.append("Status: SUCCESS\n");
        sb.append("```");
        return sb.toString();
    }

    /**
     * 결제 성공 메시지 포맷팅
     */
    private String buildPaymentSuccessMessage(Long userId, Long paymentId, String paymentMethod,
                                             Long amount, String planName) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ **결제 성공** ✅\n");
        sb.append("```\n");
        sb.append("결제 ID: ").append(paymentId).append("\n");
        sb.append("사용자 ID: ").append(userId).append("\n");
        sb.append("결제 수단: ").append(paymentMethod).append("\n");
        sb.append("플랜: ").append(planName).append("\n");
        sb.append("금액: ").append(String.format("₩%,d", amount)).append("\n");
        sb.append("시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("```");
        return ensureMaxLength(sb.toString(), 2000);
    }

    /**
     * 결제 실패 메시지 포맷팅 (단건 결제)
     */
    private String buildPaymentFailureMessage(Long userId, Long paymentId, String paymentMethod,
                                             String errorMessage, String planName) {
        StringBuilder sb = new StringBuilder();
        sb.append("❌ **결제 실패** ❌\n");
        sb.append("```\n");
        sb.append("결제 ID: ").append(paymentId).append("\n");
        sb.append("사용자 ID: ").append(userId).append("\n");
        sb.append("결제 수단: ").append(paymentMethod).append("\n");
        sb.append("플랜: ").append(planName).append("\n");
        sb.append("에러: ").append(errorMessage).append("\n");
        sb.append("시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("```");
        return ensureMaxLength(sb.toString(), 2000);
    }

    /**
     * 정기결제 성공 메시지 포맷팅 (2회차+)
     */
    private String buildSubscriptionRenewalSuccessMessage(Long userId, Long subscriptionId,
                                                         Long amount, String planName,
                                                         LocalDateTime newEndDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ **정기결제 갱신 성공** ✅\n");
        sb.append("```\n");
        sb.append("구독 ID: ").append(subscriptionId).append("\n");
        sb.append("사용자 ID: ").append(userId).append("\n");
        sb.append("플랜: ").append(planName).append("\n");
        sb.append("금액: ").append(String.format("₩%,d", amount)).append("\n");
        sb.append("신규 만료일: ").append(newEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("```");
        return ensureMaxLength(sb.toString(), 2000);
    }

    /**
     * 정기결제 실패 메시지 포맷팅 (2회차+)
     */
    private String buildSubscriptionRenewalFailureMessage(Long userId, Long subscriptionId,
                                                         String errorMessage, String planName) {
        StringBuilder sb = new StringBuilder();
        sb.append("❌ **정기결제 갱신 실패** ❌\n");
        sb.append("```\n");
        sb.append("구독 ID: ").append(subscriptionId).append("\n");
        sb.append("사용자 ID: ").append(userId).append("\n");
        sb.append("플랜: ").append(planName).append("\n");
        sb.append("에러: ").append(errorMessage).append("\n");
        sb.append("시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("```");
        return ensureMaxLength(sb.toString(), 2000);
    }

    /**
     * 메시지를 Discord 최대 길이(2000자)로 제한
     */
    private String ensureMaxLength(String message, int maxLength) {
        if (message.length() > maxLength) {
            return message.substring(0, maxLength - 20) + "\n... (생략됨)";
        }
        return message;
    }
}