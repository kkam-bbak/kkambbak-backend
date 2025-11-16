package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NoWrongVocabularyException extends CustomException {
    public NoWrongVocabularyException() {super(LearningErrorCode.NO_WRONG_VOCABULARY);}

    public NoWrongVocabularyException(String message) {super(LearningErrorCode.NO_WRONG_VOCABULARY, message);}
}
