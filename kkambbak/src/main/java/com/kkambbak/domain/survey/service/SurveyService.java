package com.kkambbak.domain.survey.service;

import com.kkambbak.domain.survey.dto.SurveyDto;



public interface SurveyService {
    // 설문 저장
    SurveyDto.SurveySaveResponse save(Long userId, SurveyDto.SurveySaveRequest req);

    // 설문 완료 여부 확인
    boolean isCompleted(Long userId);
}