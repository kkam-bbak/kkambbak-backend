package com.kkambbak.domain.survey.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SurveyErrorCode implements ResponseCode {

    INVALID_SURVEY_REQUEST("S001", "Invalid survey request"),   // 잘못된 요청
    SURVEY_ALREADY_EXISTS("S002", "Survey already exists");      // 중복 설문

    private final String statusCode;
    private final String message;
}