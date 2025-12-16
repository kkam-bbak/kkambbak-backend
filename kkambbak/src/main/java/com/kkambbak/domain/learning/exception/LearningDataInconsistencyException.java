package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class LearningDataInconsistencyException extends CustomException {

    public LearningDataInconsistencyException() {
        super(LearningErrorCode.LEARNING_DATA_INCONSISTENCY);
    }

    public LearningDataInconsistencyException(String message) {
        super(LearningErrorCode.LEARNING_DATA_INCONSISTENCY, message);
    }
}