package com.kkambbak.domain.learning.dto;

import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.LearningResult;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCardDto {

    private Long id;                 // 세션 고유 ID
    private String title;            // ex. Topik 1, Emotions
    private String categoryName;     // ex. TOPIK, CASUAL
    private int vocabularyCount;     // session_vocabularies 기준 단어 수
    private boolean completed;       // 학습 완료 여부
    private int durationSeconds;     // 학습 소요 시간(초)

    /** 학습결과 없는 기본 카드 */
    public static SessionCardDto from(Session session, int vocabCount) {
        return SessionCardDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .categoryName(session.getCategory().getType().name())
                .vocabularyCount(vocabCount)
                .completed(false)
                .durationSeconds(120)
                .build();
    }

    /** 학습결과 포함 카드 */
    public static SessionCardDto of(Session session, LearningResult result, int vocabCount) {
        return SessionCardDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .categoryName(session.getCategory().getType().name())
                .vocabularyCount(vocabCount)
                .completed(result != null)
                .durationSeconds(result != null && result.getDurationSeconds() != null
                        ? result.getDurationSeconds()
                        : 120)
                .build();
    }

    public static SessionCardDto of(Session session, int vocabCount, LearningResult result) {
        return of(session, result, vocabCount);
    }
}