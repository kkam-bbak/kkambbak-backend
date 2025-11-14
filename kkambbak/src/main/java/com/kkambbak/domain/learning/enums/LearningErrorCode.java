package com.kkambbak.domain.learning.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements ResponseCode {

    INVALID_PAGING_PARAM("L001", "Invalid paging parameter"),             // 잘못된 페이징 요청
    INCONSISTENT_EXPOSURE_RULE("L002", "Exposure rule inconsistent"),     // 상위노출 규칙 불일치
    LEARNING_QUERY_FAILURE("L003", "Failed to query learning data"),      // DB 조회 실패
    INVALID_START_PARAM("L004", "Invalid start parameters"),              // Start API 잘못된 요청
    SESSION_NOT_FOUND("L005", "Session not found"),                       // 세션이 존재하지 않음
    LEARNING_DATA_INCONSISTENCY("L006", "Learning data inconsistency"),   // 학습 데이터 구조/개수 이상
    RESULT_NOT_FOUND("L007", "Learning result not found"),                // 기존 결과 없음
    NO_WRONG_VOCABULARY("L008", "No wrong vocabulary to retry"),         // 오답이 없어 WRONG_ONLY 불가
    LEARNING_RESULT_NOT_FOUND("L009", "Learning result not found");

    private final String statusCode;
    private final String message;
}