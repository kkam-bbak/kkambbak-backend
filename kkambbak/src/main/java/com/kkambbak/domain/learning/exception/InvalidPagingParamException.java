package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidPagingParamException extends CustomException {
    public InvalidPagingParamException() {
        super(LearningErrorCode.INVALID_PAGING_PARAM);
    }
}