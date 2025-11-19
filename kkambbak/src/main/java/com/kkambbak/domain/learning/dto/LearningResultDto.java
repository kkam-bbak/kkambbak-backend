package com.kkambbak.domain.learning.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class LearningResultDto {

    @Getter
    @Builder
    public static class SummaryResponse {
        private Long sessionId;
        private Long resultId;
        private String sessionTitle;
        private int totalCount;
        private int correctCount;
        private long durationSeconds;
        private LocalDateTime completedAt;
    }

    @Getter
    @Builder
    public static class ReviewItem {
        private Long vocabularyId;
        private String korean;
        private String romanization;
        private String english;
        private boolean correct;
    }

    @Getter
    @Builder
    public static class ReviewResponse {
        private SummaryResponse summary;
        private List<ReviewItem> items;
    }
}