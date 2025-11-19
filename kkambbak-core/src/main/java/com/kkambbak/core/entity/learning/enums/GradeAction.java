package com.kkambbak.core.entity.learning.enums;

public enum GradeAction {
    GRADE,            // 발음을 실제로 채점하는 기본 동작 (정답이면 다음 단어, 오답이면 현재 단어 유지)
    NEXT_AFTER_WRONG  // 오답이 확정된 후, Next 버튼으로 정답 공개 + 다음 단어로 이동하는 동작
}
