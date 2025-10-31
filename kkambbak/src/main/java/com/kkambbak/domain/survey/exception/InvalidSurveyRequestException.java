package com.kkambbak.domain.survey.exception;

import com.kkambbak.domain.survey.enums.SurveyErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidSurveyRequestException extends CustomException {

    public InvalidSurveyRequestException() {
        super(SurveyErrorCode.INVALID_SURVEY_REQUEST);
    }
}
