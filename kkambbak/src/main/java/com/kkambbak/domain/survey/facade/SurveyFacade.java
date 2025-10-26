package com.kkambbak.domain.survey.facade;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.survey.SurveyRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SurveyFacade {

    private final UserRepository userRepository;
    private final SurveyService surveyService;
    private final SurveyRepository surveyRepository;

    public SurveyDto.SurveySaveResponse saveAndPrioritize(Long userId, SurveyDto.SurveySaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return surveyService.save(user, request);
    }

    public boolean isCompleted(Long userId) {
        return surveyRepository.existsByUser_Id(userId);
    }
}