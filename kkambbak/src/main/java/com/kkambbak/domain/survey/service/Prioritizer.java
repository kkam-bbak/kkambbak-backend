package com.kkambbak.domain.survey.service;

import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Prioritizer {

    // TOPIK: 난이도별 전체 순서
    private static final Map<DifficultyLevel, List<String>> TOPIK_RULES = Map.of(
            DifficultyLevel.BEGINNER,
            List.of("topik_1", "topik_2", "topik_3", "topik_4", "topik_5", "topik_6"),

            DifficultyLevel.INTERMEDIATE,
            List.of("topik_3", "topik_4", "topik_5", "topik_6", "topik_1", "topik_2"),

            DifficultyLevel.ADVANCED,
            List.of("topik_5", "topik_6", "topik_1", "topik_2", "topik_3", "topik_4")
    );

    // CASUAL: 관심사별 전체 순서
    private static final Map<String, List<String>> CASUAL_RULES;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("daily expressions", List.of("emotions", "places", "fruits", "body", "animals", "colors"));
        m.put("emotions",          List.of("emotions", "places", "fruits", "body", "animals", "colors"));
        m.put("food & travel",     List.of("places", "fruits", "emotions", "body", "animals", "colors"));
        m.put("test-related",      List.of("body", "animals", "emotions", "places", "fruits", "colors"));
        m.put("slang",             List.of("emotions", "colors", "places", "fruits", "body", "animals"));
        CASUAL_RULES = Collections.unmodifiableMap(m);
    }

    // Topik 전체 순서 반환
    public List<String> getTopikOrder(DifficultyLevel level) {
        DifficultyLevel safeLevel = (level != null) ? level : DifficultyLevel.BEGINNER;
        return TOPIK_RULES.getOrDefault(safeLevel, TOPIK_RULES.get(DifficultyLevel.BEGINNER));
    }

    // Casual 전체 순서 반환
    public List<String> getCasualOrder(String interests) {
        String normalized = normalizeInterest(interests);
        return CASUAL_RULES.getOrDefault(normalized, CASUAL_RULES.get("daily expressions"));
    }

    // interests 입력 정규화
    private String normalizeInterest(Object raw) {
        if (raw == null) {
            return "daily expressions";
        }

        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(v -> v.trim().toLowerCase(Locale.ROOT))
                    .filter(v -> !v.isEmpty())
                    .findFirst()
                    .orElse("daily expressions");
        }

        String result = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
        return result.isEmpty() ? "daily expressions" : result;
    }
}