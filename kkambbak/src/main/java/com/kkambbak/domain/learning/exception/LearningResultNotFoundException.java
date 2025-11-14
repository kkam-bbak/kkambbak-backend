package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class LearningResultNotFoundException extends CustomException {
    public LearningResultNotFoundException() {super(LearningErrorCode.RESULT_NOT_FOUND);}

    public LearningResultNotFoundException(String message) {super(LearningErrorCode.RESULT_NOT_FOUND, message);}
}
