package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class SessionNotFoundException extends CustomException {

    public SessionNotFoundException() {
        super(LearningErrorCode.SESSION_NOT_FOUND);
    }

    public SessionNotFoundException(String message) {
        super(LearningErrorCode.SESSION_NOT_FOUND, message);
    }
}