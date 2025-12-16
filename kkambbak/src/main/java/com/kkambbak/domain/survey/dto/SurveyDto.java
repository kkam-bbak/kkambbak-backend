package com.kkambbak.domain.survey.dto;


import com.kkambbak.core.entity.survey.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

public class SurveyDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveySaveRequest {
        // 전체 설문 응답
        @NotNull
        private Map<String, Object> rawResponses;
    }
}