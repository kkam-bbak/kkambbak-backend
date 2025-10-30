package com.kkambbak.client.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DiscordClient {

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public DiscordClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 스케줄러 에러를 디스코드로 전송
     */
    public void sendSchedulerError(String jobName, Exception exception) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Discord webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildErrorMessage(jobName, exception);
            sendMessage(message);
        } catch (Exception e) {
            log.error("Failed to send Discord webhook notification", e);
        }
    }

    /**
     * 스케줄러 성공 알림을 디스코드로 전송
     */
    public void sendSchedulerSuccess(String jobName, double executionTimeSeconds) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Discord webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            String message = buildSuccessMessage(jobName, executionTimeSeconds);
            sendMessage(message);
        } catch (Exception e) {
            log.error("Failed to send Discord webhook notification", e);
        }
    }

    /**
     * 디스코드 메시지 전송
     */
    private void sendMessage(String content) {
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
        sb.append("\n--- Stack Trace ---\n");
        sb.append(getStackTraceString(exception));
        sb.append("```");
        return sb.toString();
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
     * StackTrace를 문자열로 변환
     */
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Discord 메시지 길이 제한을 고려해서 잘라냄
        if (stackTrace.length() > 1500) {
            return stackTrace.substring(0, 1500) + "\n... (truncated)";
        }
        return stackTrace;
    }
}