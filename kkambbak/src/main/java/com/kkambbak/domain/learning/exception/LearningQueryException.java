package com.kkambbak.domain.learning.exception;


import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

/**
 * DB 조회나 내부 쿼리 실행 중 예외 발생
 */
public class LearningQueryException extends CustomException {
    public LearningQueryException() {
        super(LearningErrorCode.LEARNING_QUERY_FAILURE);
    }
}