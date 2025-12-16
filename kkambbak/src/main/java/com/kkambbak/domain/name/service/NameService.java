package com.kkambbak.domain.name.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.core.entity.name.NameHistory;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.name.NameHistoryRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.auth.exception.UserNotFoundException;
import com.kkambbak.domain.name.dto.NameCandidateItemDto;
import com.kkambbak.domain.name.exception.NameCandidateParseException;
import com.kkambbak.domain.name.exception.NameGenerationSaveException;
import com.kkambbak.domain.name.exception.NameHistoryForbiddenException;
import com.kkambbak.domain.name.exception.NameHistoryNotFoundException;
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
    private final UserRepository userRepository;

    public List<NameCandidateItemDto> parseCandidates(ChatResponseDto response) {
        try {
            String content = response.getChoices().get(0).getMessage().getContent();
            JsonNode rootNode = objectMapper.readTree(content);
            JsonNode namesNode = rootNode.get("names");

            if (namesNode == null || !namesNode.isArray()) {
                log.error("GPT 응답에서 'names' 배열이 없습니다.");
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
            log.error("이름 생성 결과 저장 실패", e);
            throw new NameGenerationSaveException();
        }
    }

    public int calculateNextAttempt(Long userId) {
        int count = nameHistoryRepository.countByUser_Id(userId);
        return count + 1;
    }


    public NameHistory validateHistoryOfUser(Long userId, Long historyId) {
        NameHistory history = nameHistoryRepository.findById(historyId)
                .orElseThrow(NameHistoryNotFoundException::new);

        if (!history.getUser().getId().equals(userId)) {
            throw new NameHistoryForbiddenException();
        }
        return history;
    }

    @Transactional
    public void selectName(NameHistory history, String koreanName, String nameMeaning) {
        User user = history.getUser();
        history.markSelected();
        user.updateKoreanName(koreanName, nameMeaning);
    }

    public NameHistory getLatestHistory(Long userId) {
        return nameHistoryRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(NameHistoryNotFoundException::new);
    }

    public List<NameCandidateItemDto> parseGenerationOutput(Map<String, Object> generationOutput) {
        try {
            if (generationOutput == null || !generationOutput.containsKey("names")) {
                log.error("생성 결과에 'names' 키가 없습니다.");
                throw new NameCandidateParseException();
            }
            Object namesObj = generationOutput.get("names");
            return objectMapper.convertValue(
                    namesObj,
                    new TypeReference<List<NameCandidateItemDto>>() {}
            );
        } catch (NameCandidateParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("이름 생성 결과 파싱 실패", e);
            throw new NameCandidateParseException();
        }
    }

    public int getGenerationCount(Long userId) {
        return calculateNextAttempt(userId) - 1;
    }
}

