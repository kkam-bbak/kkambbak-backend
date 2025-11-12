package com.kkambbak.domain.roleplay.service;

import com.kkambbak.client.openai.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleplayCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "roleplay:session:";
    private static final Duration CACHE_TTL = Duration.ofHours(3);

    private String key(Long sessionId) {
        return PREFIX + sessionId;
    }

    public void saveMessages(Long sessionId, List<ChatMessage> messages) {
        String key = key(sessionId);
        redisTemplate.opsForValue().set(key, messages,CACHE_TTL);
    }

    @SuppressWarnings("unchecked")
    public List<ChatMessage> getMessages(Long sessionId) {
        Object value = redisTemplate.opsForValue().get(key(sessionId));
        if (value instanceof List<?>) {
            return (List<ChatMessage>) value;
        }
        return Collections.emptyList();
    }


    public void clear(Long sessionId) {
        redisTemplate.delete(key(sessionId));
    }
}
