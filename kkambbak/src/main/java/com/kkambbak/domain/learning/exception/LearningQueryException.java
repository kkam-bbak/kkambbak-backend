package com.kkambbak.domain.learning.exception;


import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class LearningQueryException extends CustomException {
    public LearningQueryException() {
        super(LearningErrorCode.LEARNING_QUERY_FAILURE);
    }
}