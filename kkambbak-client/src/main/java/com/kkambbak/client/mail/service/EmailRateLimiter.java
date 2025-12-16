package com.kkambbak.client.mail.service;

import com.kkambbak.client.mail.exception.EmailRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_EMAILS_PER_HOUR = 10;
    private static final long HOUR_IN_SECONDS = 3600;
    private static final String EMAIL_RATE_LIMIT_KEY_PREFIX = "email:rate:limit:";

    /**
     * 이메일 발송 가능 여부 확인 및 카운트 증가
     */
    public void checkAndIncrementEmailCount(String email) {
        String key = EMAIL_RATE_LIMIT_KEY_PREFIX + email;

        try {
            String countStr = redisTemplate.opsForValue().get(key);
            long currentCount = countStr != null ? Long.parseLong(countStr) : 0L;

            if (currentCount >= MAX_EMAILS_PER_HOUR) {
                log.warn("[EmailRateLimiter] Email limit exceeded for user: {}, count: {}", email, currentCount);
                throw new EmailRateLimitExceededException();
            }

            Long newCount = redisTemplate.opsForValue().increment(key);

            if (newCount != null && newCount.equals(1L)) {
                redisTemplate.expire(key, HOUR_IN_SECONDS, TimeUnit.SECONDS);
            }

        } catch (EmailRateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EmailRateLimiter] Error checking rate limit for email: {}", email, e);
        }
    }
}