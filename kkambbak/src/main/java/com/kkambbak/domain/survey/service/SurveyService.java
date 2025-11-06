package com.kkambbak.domain.survey.service;

import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.survey.SurveyRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.exception.InvalidSurveyRequestException;
import com.kkambbak.domain.survey.exception.SurveyAlreadyExistsException;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    // 설문 저장
    @Transactional
    public void save(Long userId, SurveyDto.SurveySaveRequest req) {
        // 요청 검증
        if (req == null
                || req.getLevel() == null
                || req.getInterests() == null || req.getInterests().isBlank()
                || req.getRawResponses() == null || req.getRawResponses().isBlank()) {
            throw new InvalidSurveyRequestException();
        }

        // 사용자 존재 여부
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 중복 방지
        if (surveyRepository.existsByUser_Id(user.getId())) {
            throw new SurveyAlreadyExistsException();
        }

        // 저장
        try {
            surveyRepository.save(
                    Survey.builder()
                            .user(user)
                            .responses(req.getRawResponses().trim()) // String 그대로
                            .build()
            );
            log.info("설문 저장 완료 userId={}", user.getId());
        } catch (DataIntegrityViolationException e) {
            throw new SurveyAlreadyExistsException();
        }
    }

    // 설문 완료 여부 확인
    public boolean isCompleted(Long userId) {
        return surveyRepository.existsByUser_Id(userId);
    }
}