package com.kkambbak.domain.survey.service;

import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.domain.survey.dto.SurveyDto;

import java.util.Optional;

public interface SurveyService {
    // 설문 저장 + 상위 노출 계산
    SurveyDto.SurveySaveResponse save(User user, SurveyDto.SurveySaveRequest req);
}