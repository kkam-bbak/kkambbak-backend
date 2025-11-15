package com.kkambbak.domain.learning.dto;

import com.kkambbak.core.entity.learning.enums.GradeAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 발음 학습 채점 API
 *
 * - POST /api/v1/learning/grade
 * - action = GRADE           : 정답일때 다음 단어로 이동, 오답일 때 try agin 버튼 누르면 다음 단어로 이동
 * - action = NEXT_AFTER_WRONG: 오답일때 next버튼 누르면 오답 확정 + 다음 단어로 이동
 */
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
        private String audio;
    }

    // ===================== Response =====================

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeResponse {

        private boolean correct;

        /**
         *  이번 grade 요청으로 학습 포커스가 다음 단어로 넘어갔는지 여부
         * - true  : 다음 단어로 이동
         * - false : 현재 단어에 그대로 머무름
         */
        private boolean moved;

        /**
         * 현재 세션의 학습을 모두 끝냈는지 여부
         * - true  : 마지막 단어까지 처리 완료
         * - false : 아직 남은 단어가 있음
         */
        private boolean finished;

        /**
         * 다음에 학습할 단어 정보
         * - moved == true && finished == false 인 경우 채워짐
         * - 마지막 단어를 끝낸 경우(finished == true)에는 null 가능
         */
        private Next next;

        // 오답 확정 시, 정답을 보여주기 위한 정보
        private CorrectAnswer correctAnswer;

        public static GradeResponse of(boolean correct,
                                       boolean moved,
                                       boolean finished,
                                       Next next,
                                       CorrectAnswer correctAnswer) {
            return GradeResponse.builder()
                    .correct(correct)
                    .moved(moved)
                    .finished(finished)
                    .next(next)
                    .correctAnswer(correctAnswer)
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