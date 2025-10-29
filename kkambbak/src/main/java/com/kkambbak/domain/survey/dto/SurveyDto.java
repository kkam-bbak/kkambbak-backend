package com.kkambbak.domain.survey.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
        @NotNull
        @NotEmpty
        private Map<String, Object> rawResponses;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SurveySaveResponse {
        private Long surveyId;
        private Boolean completed;
        private LocalDateTime createdAt;
    }
}