package com.kkambbak.domain.learning.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements ResponseCode {

    INVALID_PAGING_PARAM("L001", "Invalid paging parameter"),             // 잘못된 페이징 요청
    INCONSISTENT_EXPOSURE_RULE("L002", "Exposure rule inconsistent"),     // 상위노출 규칙 불일치
    LEARNING_QUERY_FAILURE("L003", "Failed to query learning data");      // DB 조회 실패

    private final String statusCode;
    private final String message;
}