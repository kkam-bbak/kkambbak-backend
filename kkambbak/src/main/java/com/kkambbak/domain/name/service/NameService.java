package com.kkambbak.domain.name.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.core.entity.name.NameHistory;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.name.NameHistoryRepository;
import com.kkambbak.domain.name.dto.NameCandidateItemDto;
import com.kkambbak.domain.name.exception.NameCandidateParseException;
import com.kkambbak.domain.name.exception.NameGenerationSaveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NameService {
    private final NameHistoryRepository nameHistoryRepository;
    private final ObjectMapper objectMapper;


    public List<NameCandidateItemDto> parseCandidates(ChatResponseDto response) {
        try {
            String content = response.getChoices().get(0).getMessage().getContent();
            JsonNode rootNode = objectMapper.readTree(content);
            JsonNode namesNode = rootNode.get("names");

            if (namesNode == null || !namesNode.isArray()) {
                log.error("GPT 응답에서 'names' 배열이 없습니다");
                throw new NameCandidateParseException();
            }
            return objectMapper.convertValue(
                    namesNode,
                    new TypeReference<List<NameCandidateItemDto>>() {}
            );

        } catch (Exception e) {
            log.error("GPT 이름 후보 파싱 실패", e);
            throw new NameCandidateParseException();
        }
    }

    @Transactional
    public NameHistory generate(User user, List<NameCandidateItemDto> candidates,int attempt) {
        try {
            Map<String, Object> jsonMap = Map.of("names", candidates);
            NameHistory history = NameHistory.create(user, jsonMap,attempt);
            nameHistoryRepository.save(history);
            return history;

        } catch (Exception e) {
            throw new NameGenerationSaveException();
        }
    }

    public int calculateNextAttempt(Long userId) {
        int count = nameHistoryRepository.countByUser_Id(userId);
        return count + 1;
    }
}
