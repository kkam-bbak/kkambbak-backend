package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidGradeAttemptException extends CustomException {

    public InvalidGradeAttemptException() {
        super(LearningErrorCode.INVALID_GRADE_ATTEMPT);
    }

    public InvalidGradeAttemptException(String message) {
        super(LearningErrorCode.INVALID_GRADE_ATTEMPT, message);
    }
}