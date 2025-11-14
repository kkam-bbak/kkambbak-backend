package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NoWrongVocabularyException extends CustomException {
    public NoWrongVocabularyException() {super(LearningErrorCode.RESULT_NOT_FOUND);}

    public NoWrongVocabularyException(String message) {super(LearningErrorCode.RESULT_NOT_FOUND, message);}
}
