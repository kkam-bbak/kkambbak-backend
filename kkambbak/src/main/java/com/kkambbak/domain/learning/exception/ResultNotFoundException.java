package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ResultNotFoundException extends CustomException {
    public ResultNotFoundException() {super(LearningErrorCode.NO_WRONG_VOCABULARY);}

    public ResultNotFoundException(String message) {super(LearningErrorCode.NO_WRONG_VOCABULARY, message);}
}
