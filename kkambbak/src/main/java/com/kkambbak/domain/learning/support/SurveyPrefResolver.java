package com.kkambbak.domain.learning.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.survey.enums.DifficultyLevel;
import com.kkambbak.core.entity.survey.enums.InterestType;
import com.kkambbak.core.repository.survey.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyPrefResolver {

    private final SurveyRepository surveyRepository;
    private final ObjectMapper objectMapper;

    // 설문에서 난이도와 관심사를 가져옴(성공 시 Optional.of, 실패 시 Optional.empty)
    public Optional<UserPref> resolve(Long userId) {
        if (userId == null) return Optional.empty();

        Optional<Survey> surveyOpt = surveyRepository.findByUser_Id(userId);
        if (surveyOpt.isEmpty()) return Optional.empty();

        try {
            String raw = surveyOpt.get().getResponses();
            JsonNode root = objectMapper.readTree(raw);

            String diffStr = getCaseInsensitive(root,
                    "Which level suits you best?", "difficulty", "difficultyLevel");
            String interestStr = getCaseInsensitive(root,
                    "What kind of words are you most interested in?", "interestType", "interest");

            if (diffStr == null || interestStr == null) return Optional.empty();

            DifficultyLevel diff = DifficultyLevel.valueOf(
                    diffStr.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
            InterestType interest = InterestType.valueOf(
                    interestStr.trim().toUpperCase().replace(" ", "_").replace("-", "_"));

            return Optional.of(new UserPref(diff, interest));
        } catch (Exception e) {
            log.warn("Failed to resolve survey preferences for userId={}", userId, e);
            return Optional.empty();
        }
    }

    private String getCaseInsensitive(JsonNode root, String... keys) {
        for (String k : keys) {
            JsonNode n = root.get(k);
            if (n != null && !n.isNull() && n.isTextual()) {
                return n.asText();
            }
        }
        return null;
    }

    // 설문에서 가져온 난이도와 관심사
    public record UserPref(DifficultyLevel difficultyLevel, InterestType interestType) {}
}
