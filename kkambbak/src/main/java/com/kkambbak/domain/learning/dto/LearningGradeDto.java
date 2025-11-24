package com.kkambbak.domain.learning.dto;

import com.kkambbak.core.entity.learning.enums.GradeAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class LearningGradeDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeRequest {

        @NotNull
        private GradeAction action;  // GRADE / NEXT_AFTER_WRONG

        // 현재 채점중인 단어 id (Vocabulary.id = itemId)
        @NotNull
        private Long itemId;

        @NotNull
        private Long resultId;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeResponse {
        private boolean correct;
        private boolean moved;  //다음 문제로 넘어갈 수 있는지 여부
        private boolean finished; // 학습이 끝났는지 여부
        private Next next; // 다음 문제에서 학습할 단어 정보
        private CorrectAnswer correctAnswer; // 오답 확정 시, 정답에 대한 단어의 정보를 보여주기 위해
        private Double score;

        public static GradeResponse of(boolean correct,
                                       boolean moved,
                                       boolean finished,
                                       Next next,
                                       CorrectAnswer correctAnswer,
                                       Double score) {
            return GradeResponse.builder()
                    .correct(correct)
                    .moved(moved)
                    .finished(finished)
                    .next(next)
                    .correctAnswer(correctAnswer)
                    .score(score)
                    .build();
        }
    }


    // 다음에 학습할 단어 정보
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Next {
        private Long itemId;
        private String korean;
        private String romanization;
        private String english;
        private String imageUrl;
    }

    // 오답 확정 후, 정답을 보여주는 용도
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CorrectAnswer {
        private Long itemId;
        private String korean;
        private String romanization;
        private String english;
        private String imageUrl;
    }
}