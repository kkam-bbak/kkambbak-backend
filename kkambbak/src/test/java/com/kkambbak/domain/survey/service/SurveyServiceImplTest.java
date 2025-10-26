package com.kkambbak.domain.survey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.survey.SurveyRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.CategoryType;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SurveyServiceImplTest {

    private SurveyRepository surveyRepository;
    private Prioritizer prioritizer;
    private ObjectMapper objectMapper;
    private SurveyServiceImpl service;

    // 공통 더미
    private User mockUser;

    @BeforeEach
    void setUp() {
        surveyRepository = mock(SurveyRepository.class);
        prioritizer = mock(Prioritizer.class);
        objectMapper = new ObjectMapper();
        service = new SurveyServiceImpl(surveyRepository, prioritizer, objectMapper);

        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
    }

    private SurveyDto.SurveySaveResponse.Priorities topikPriorities(DifficultyLevel level, List<String> topics) {
        return SurveyDto.SurveySaveResponse.Priorities.builder()
                .categoryType(CategoryType.TOPIK)
                .level(level)
                .topics(topics)
                .build();
    }

    @Nested
    class Save {

        @Test
        @DisplayName("신규 설문 저장: TOPIK 기준 우선노출 계산 + DB 저장 + 응답 반환")
        void save_newSurvey_ok() {
            // given
            var req = SurveyDto.SurveySaveRequest.builder()
                    .level(DifficultyLevel.BEGINNER)
                    .interests("daily expressions")
                    .build();

            when(surveyRepository.existsByUser(mockUser)).thenReturn(false);
            when(prioritizer.buildForTopik(DifficultyLevel.BEGINNER))
                    .thenReturn(topikPriorities(DifficultyLevel.BEGINNER, List.of("topik_1", "topik_2")));

            // when
            SurveyDto.SurveySaveResponse resp = service.save(mockUser, req);

            // then
            assertTrue(resp.isCompleted());
            assertEquals(CategoryType.TOPIK, resp.getPriorities().getCategoryType());
            assertEquals(DifficultyLevel.BEGINNER, resp.getPriorities().getLevel());
            assertEquals(List.of("topik_1", "topik_2"), resp.getPriorities().getTopics());

            // DB 저장 호출 확인
            verify(surveyRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("이미 설문 존재: 저장 스킵하고 우선노출 결과만 반환")
        void save_exists_skipStore() {
            // given
            var req = SurveyDto.SurveySaveRequest.builder()
                    .level(DifficultyLevel.INTERMEDIATE)
                    .interests("food & travel")
                    .build();

            when(surveyRepository.existsByUser(mockUser)).thenReturn(true);
            when(prioritizer.buildForTopik(DifficultyLevel.INTERMEDIATE))
                    .thenReturn(topikPriorities(DifficultyLevel.INTERMEDIATE, List.of("topik_3", "topik_4")));

            // when
            var resp = service.save(mockUser, req);

            // then
            assertTrue(resp.isCompleted());
            assertEquals(CategoryType.TOPIK, resp.getPriorities().getCategoryType());
            assertEquals(DifficultyLevel.INTERMEDIATE, resp.getPriorities().getLevel());
            assertEquals(List.of("topik_3", "topik_4"), resp.getPriorities().getTopics());

            verify(surveyRepository, never()).save(any()); // 저장 스킵
        }

        @Test
        @DisplayName("요청에 rawResponses가 들어오면 그것을 그대로 DB에 저장 (level/interests만으로 재구성하지 않음)")
        void save_usesRawResponses_whenProvided() throws Exception {
            // given
            Map<String, Object> raw = Map.of(
                    "level", "BEGINNER",
                    "interests", "daily expressions",
                    "q3", "extra-1",
                    "q4", Map.of("k", "v")
            );
            var req = SurveyDto.SurveySaveRequest.builder()
                    .level(DifficultyLevel.BEGINNER)
                    .interests("daily expressions")
                    .rawResponses(raw)
                    .build();

            when(surveyRepository.existsByUser(mockUser)).thenReturn(false);
            when(prioritizer.buildForTopik(DifficultyLevel.BEGINNER))
                    .thenReturn(topikPriorities(DifficultyLevel.BEGINNER, List.of("topik_1", "topik_2")));

            ArgumentCaptor<com.kkambbak.core.entity.survey.Survey> captor = ArgumentCaptor.forClass(com.kkambbak.core.entity.survey.Survey.class);

            // when
            var resp = service.save(mockUser, req);

            // then
            assertTrue(resp.isCompleted());
            verify(surveyRepository).save(captor.capture());
            var saved = captor.getValue();

            // 저장된 JSON 다시 역직렬화해서 raw와 동일한지 확인
            Map<?, ?> savedMap = objectMapper.readValue(saved.getResponses(), Map.class);
            assertEquals(raw, savedMap);
        }

        @Test
        @DisplayName("DB Unique 제약(DataIntegrityViolation) 발생해도 예외 터뜨리지 않고 결과 반환")
        void save_handlesUniqueViolation() {
            // given
            var req = SurveyDto.SurveySaveRequest.builder()
                    .level(DifficultyLevel.ADVANCED)
                    .interests("slang")
                    .build();

            when(surveyRepository.existsByUser(mockUser)).thenReturn(false);
            when(prioritizer.buildForTopik(DifficultyLevel.ADVANCED))
                    .thenReturn(topikPriorities(DifficultyLevel.ADVANCED, List.of("topik_5", "topik_6")));

            doThrow(new DataIntegrityViolationException("dup")).when(surveyRepository).save(any());

            // when
            var resp = service.save(mockUser, req);

            // then
            assertTrue(resp.isCompleted());
            assertEquals(List.of("topik_5", "topik_6"), resp.getPriorities().getTopics());
            verify(surveyRepository, times(1)).save(any()); // 시도는 함
        }

        @Test
        @DisplayName("인증 없음 → IllegalStateException")
        void save_noAuth_throws() {
            var req = SurveyDto.SurveySaveRequest.builder()
                    .level(DifficultyLevel.BEGINNER)
                    .interests("daily expressions")
                    .build();

            assertThrows(IllegalStateException.class, () -> service.save(null, req));
        }

        @Test
        @DisplayName("필수 필드 누락(level/interests) → IllegalArgumentException")
        void save_invalidRequest_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.save(mockUser, SurveyDto.SurveySaveRequest.builder().build()));

            assertThrows(IllegalArgumentException.class,
                    () -> service.save(mockUser, SurveyDto.SurveySaveRequest.builder()
                            .level(DifficultyLevel.BEGINNER).build()));

            assertThrows(IllegalArgumentException.class,
                    () -> service.save(mockUser, SurveyDto.SurveySaveRequest.builder()
                            .interests("  ").level(DifficultyLevel.BEGINNER).build()));
        }
    }

    @Nested
    class Exists {

        @Test
        @DisplayName("isCompleted: user==null → false")
        void isCompleted_nullUser_false() {
            assertFalse(service.isCompleted(null));
        }

        @Test
        @DisplayName("isCompleted: 레코드 존재 true/false 반환")
        void isCompleted_delegatesToRepository() {
            when(surveyRepository.existsByUser(mockUser)).thenReturn(true);
            assertTrue(service.isCompleted(mockUser));

            when(surveyRepository.existsByUser(mockUser)).thenReturn(false);
            assertFalse(service.isCompleted(mockUser));
        }
    }
}