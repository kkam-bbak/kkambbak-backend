package com.kkambbak.domain.survey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.survey.enums.DifficultyLevel;
import com.kkambbak.core.entity.survey.enums.InterestType;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;


    // 설문 저장
    @Transactional
    public void save(Long userId, SurveyDto.SurveySaveRequest req) {
        // 요청 검증
        if (req == null || req.getRawResponses() == null || req.getRawResponses().isEmpty()) {
            throw new InvalidSurveyRequestException();
        }

        // 답변 유효성 검증
        validateSurveyResponses(req.getRawResponses());

        // 사용자 존재 여부
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 중복 방지
        if (surveyRepository.existsByUser_Id(user.getId())) {
            throw new SurveyAlreadyExistsException();
        }

        final String normalizedJson = toCanonicalJsonOrThrow(req.getRawResponses());


        // 저장
        try {
            surveyRepository.save(
                    Survey.builder()
                            .user(user)
                            .responses(normalizedJson)
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

    // 설문 응답 유효성 검증
    // Todo: 추후 필요 시 나머지 설문 답변도 이넘으로 관리 후 검증 필요
    private void validateSurveyResponses(Map<String, Object> responses) {
        // 난이도 질문 검증
        Object difficultyAnswer = responses.get("Which level suits you best?");
        if (difficultyAnswer != null) {
            String normalized = normalizeAnswer(difficultyAnswer.toString());
            try {
                DifficultyLevel.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid difficulty answer: {}", difficultyAnswer);
                throw new InvalidSurveyRequestException("Invalid answer format for difficulty question");
            }
        }

        // 관심사 질문 검증
        Object interestAnswer = responses.get("What kind of words are you most interested in?");
        if (interestAnswer != null) {
            String normalized = normalizeAnswer(interestAnswer.toString());
            try {
                InterestType.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid interest answer: {}", interestAnswer);
                throw new InvalidSurveyRequestException("Invalid answer format for interest question");
            }
        }
    }

    // 답변을 Enum 형식으로 정규화 (공백, 하이픈을 언더스코어로 변환)
    private String normalizeAnswer(String answer) {
        return answer.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
    }

    // Map을 Json 문자열로 바꿔 저장 가능한 형태로
    private String toCanonicalJsonOrThrow(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new InvalidSurveyRequestException();
        }
    }
}