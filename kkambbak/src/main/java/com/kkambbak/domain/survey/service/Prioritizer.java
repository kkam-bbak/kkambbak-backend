package com.kkambbak.domain.survey.service;

import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.CategoryType;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Prioritizer {

    // TOPIK: 난이도별 토픽
    private static final Map<DifficultyLevel, List<String>> TOPIK_RULES = Map.of(
            DifficultyLevel.BEGINNER,     List.of("topik_1", "topik_2"),
            DifficultyLevel.INTERMEDIATE, List.of("topik_3", "topik_4"),
            DifficultyLevel.ADVANCED,     List.of("topik_5", "topik_6")
    );

    // CASUAL: 관심사별 토픽
    private static final Map<String, List<String>> CASUAL_RULES;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("daily expressions", List.of("emotions", "places"));
        m.put("emotions",          List.of("emotions"));
        m.put("food & travel",     List.of("fruits", "places"));
        m.put("test-related",      List.of("body", "animals"));
        m.put("slang",             List.of("emotions", "colors"));
        CASUAL_RULES = Collections.unmodifiableMap(m);
    }

     // 저장 직후/Topik 탭 기본 노출용.
    public SurveyDto.SurveySaveResponse.Priorities buildForTopik(DifficultyLevel level) {
        DifficultyLevel safeLevel = (level == null) ? DifficultyLevel.BEGINNER : level;
        List<String> topics = TOPIK_RULES.getOrDefault(safeLevel, TOPIK_RULES.get(DifficultyLevel.BEGINNER));

        return SurveyDto.SurveySaveResponse.Priorities.builder()
                .categoryType(CategoryType.TOPIK)
                .level(safeLevel)
                .topics(topics)
                .build();
    }

    // CASUAL 탭에서 사용.
    public SurveyDto.SurveySaveResponse.Priorities buildForCasual(String interestRaw) {
        String interest = normalizeInterest(interestRaw);
        List<String> topics = CASUAL_RULES.getOrDefault(interest, List.of("emotions", "places"));

        return SurveyDto.SurveySaveResponse.Priorities.builder()
                .categoryType(CategoryType.CASUAL)
                .level(null) // CASUAL은 난이도 미사용
                .topics(topics)
                .build();
    }


    // interests 입력을 단일 문자열로 정규화
    private String normalizeInterest(Object raw) {
        if (raw == null) return "";
        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(v -> v.trim().toLowerCase(Locale.ROOT))
                    .filter(v -> !v.isEmpty())
                    .findFirst()
                    .orElse("");
        }
        return String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
    }
}