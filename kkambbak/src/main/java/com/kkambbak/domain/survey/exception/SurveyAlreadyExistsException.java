package com.kkambbak.domain.survey.exception;

import com.kkambbak.domain.survey.enums.SurveyErrorCode;
import com.kkambbak.global.exception.CustomException;

public class SurveyAlreadyExistsException extends CustomException {

    public SurveyAlreadyExistsException() {
        super(SurveyErrorCode.SURVEY_ALREADY_EXISTS);
    }
}
