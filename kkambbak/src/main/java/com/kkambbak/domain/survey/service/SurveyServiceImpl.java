package com.kkambbak.domain.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.survey.SurveyRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private static final String K_LEVEL = "level";
    private static final String K_INTERESTS = "interests";

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SurveyDto.SurveySaveResponse save(Long userId, SurveyDto.SurveySaveRequest req) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 2. 요청 검증
        if (req == null || req.getLevel() == null || req.getInterests() == null || req.getInterests().isBlank()) {
            throw new IllegalArgumentException("설문 필드 'level'과 'interests'는 비어 있을 수 없습니다.");
        }

        // 3. 설문 답변 데이터 구성
        Map<String, Object> surveyResponses = new LinkedHashMap<>();
        if (req.getRawResponses() != null) {
            surveyResponses.putAll(req.getRawResponses());
        }
        surveyResponses.putIfAbsent(K_LEVEL, req.getLevel().name());
        surveyResponses.putIfAbsent(K_INTERESTS, req.getInterests().trim());

        // 4. 기존 설문 확인
        Optional<Survey> existingSurvey = surveyRepository.findByUser_Id(user.getId());
        if (existingSurvey.isPresent()) {
            log.debug("이미 설문이 존재합니다. userId={}", user.getId());
            Survey survey = existingSurvey.get();
            return SurveyDto.SurveySaveResponse.builder()
                    .surveyId(survey.getId())
                    .completed(true)
                    .createdAt(survey.getCreatedAt())
                    .build();
        }

        // 5. DB 저장
        Survey savedSurvey = null;
        try {
            String json = toJson(surveyResponses);
            savedSurvey = surveyRepository.save(
                    Survey.builder()
                            .user(user)
                            .responses(json)
                            .build()
            );
            log.info("설문이 저장되었습니다. userId={}", user.getId());
        } catch (DataIntegrityViolationException e) {
            log.warn("설문 저장 중 유니크 제약 충돌 발생. userId={}, 기존 설문을 조회합니다.", user.getId());
            // 충돌 시 기존 설문 조회
            savedSurvey = surveyRepository.findByUser_Id(user.getId()).orElse(null);
        }

        // 6. 응답 반환
        return SurveyDto.SurveySaveResponse.builder()
                .surveyId(savedSurvey != null ? savedSurvey.getId() : null)
                .completed(true)
                .createdAt(savedSurvey != null ? savedSurvey.getCreatedAt() : null)
                .build();
    }

    @Override
    public boolean isCompleted(Long userId) {
        return surveyRepository.existsByUser_Id(userId);
    }

    /**
     * Map → JSON 문자열 변환
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("설문 페이로드를 JSON으로 변환하는 중 오류가 발생했습니다.", e);
        }
    }
}