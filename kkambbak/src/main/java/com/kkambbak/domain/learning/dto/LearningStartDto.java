package com.kkambbak.domain.learning.dto;

import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.enums.LearningMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class LearningStartDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartRequest {
        @Builder.Default
        private LearningMode mode = LearningMode.ALL;

        private Long baseResultId; // 틀린 것만 학습(WRONG_ONLY)시 기준이 되는 결과 id
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartResponse {
        private Long sessionId;
        private Long resultId; // 새로 생성된 학습 결과 ID
        private List<Long> vocabIds;
        private int totalVocabularyCount;
        private Long baseResultId;
        private FirstVocabulary firstVocabulary;
        private String sessionTitle;

        public static StartResponse of(Long sessionId,
                                       Long resultId,
                                       List<Long> vocabIds,
                                       FirstVocabulary firstVocabulary,
                                       Long baseResultId,
                                       Session session) {
            return StartResponse.builder()
                    .sessionId(sessionId)
                    .resultId(resultId)
                    .vocabIds(vocabIds)
                    .sessionTitle(session.getTitle())
                    .totalVocabularyCount(vocabIds == null ? 0 : vocabIds.size())
                    .firstVocabulary(firstVocabulary)
                    .baseResultId(baseResultId)
                    .build();
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class FirstVocabulary {
            private Long vocabularyId;
            private String korean;
            private String romanization;
            private String english;
            private String imageUrl;
        }
    }
}