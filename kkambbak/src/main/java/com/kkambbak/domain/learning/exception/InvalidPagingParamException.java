package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

/**
 * 잘못된 페이징 요청 파라미터
 */
public class InvalidPagingParamException extends CustomException {
    public InvalidPagingParamException() {
        super(LearningErrorCode.INVALID_PAGING_PARAM);
    }
}