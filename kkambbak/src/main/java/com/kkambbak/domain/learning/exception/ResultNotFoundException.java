package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ResultNotFoundException extends CustomException {
    public ResultNotFoundException() {super(LearningErrorCode.RESULT_NOT_FOUND);}

    public ResultNotFoundException(String message) {super(LearningErrorCode.RESULT_NOT_FOUND, message);}
}
