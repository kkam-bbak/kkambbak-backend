package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidStartParamException extends CustomException {

    public InvalidStartParamException() {
        super(LearningErrorCode.INVALID_START_PARAM);
    }

    public InvalidStartParamException(String message) {
        super(LearningErrorCode.INVALID_START_PARAM, message);
    }
}