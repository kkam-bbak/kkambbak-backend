package com.kkambbak.domain.survey.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kkambbak.domain.survey.enums.CategoryType;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SurveyDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveySaveRequest {
        @NotNull
        private DifficultyLevel level;

        @NotBlank
        private String interests;

        // 전체 설문 응답
        private Map<String, Object> rawResponses;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SurveySaveResponse {
        private Long surveyId;
        private boolean completed;
        private Priorities priorities;
        private LocalDateTime createdAt;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Priorities {
            private CategoryType categoryType;
            private DifficultyLevel level;
            private List<String> topics; // 상위 노출될 주제 리스트 예: ["topik_1", "topik_2"] or ["emotions", "travel"]
        }
    }
}