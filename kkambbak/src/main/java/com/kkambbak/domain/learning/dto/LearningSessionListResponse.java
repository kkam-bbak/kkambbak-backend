package com.kkambbak.domain.learning.dto;

import com.kkambbak.core.entity.survey.enums.CategoryType;
import lombok.*;

import java.util.List;

// 카테고리별 학습 세션 목록 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningSessionListResponse {

    private String categoryName;                // 카테고리명 (TOPIK / CASUAL)
    private List<SessionCardDto> sessions;      // 세션 카드 리스트
    private Long nextCursor;                    // 다음 페이지 요청 시 사용할 커서 (null이면 더 없음)
    private boolean hasNext;                    // 다음 페이지 존재 여부

    // 기본 생성 메서드 (커서 없는 초기 페이지)
    public static LearningSessionListResponse of(CategoryType categoryType, List<SessionCardDto> sessions) {
        return LearningSessionListResponse.builder()
                .categoryName(categoryType.name())
                .sessions(sessions)
                .nextCursor(null)    // 첫 페이지는 기본 null
                .hasNext(false)      // 기본 false
                .build();
    }

    // 커서 기반 생성 메서드 (무한 스크롤 지원)
    public static LearningSessionListResponse of(CategoryType categoryType,
                                                 List<SessionCardDto> sessions,
                                                 Long nextCursor,
                                                 boolean hasNext) {
        return LearningSessionListResponse.builder()
                .categoryName(categoryType.name())
                .sessions(sessions)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}