package com.kkambbak.domain.survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.survey.SurveyRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceImplTest {

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    UserRepository userRepository;

    ObjectMapper objectMapper;

    @InjectMocks
    SurveyServiceImpl sut; // system under test

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper(); // 실제 파서 사용
        sut = new SurveyServiceImpl(surveyRepository, userRepository, objectMapper);
    }

    private User mkUser(long id) {
        // User 엔티티가 @Builder 라면 아래처럼 필요 필드만
        return User.builder().id(id).build();
    }

    private SurveyDto.SurveySaveRequest mkReq() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("Which level suits you best?", "BEGINNER");
        raw.put("What kind of words are you into recently?", "daily expressions");
        return SurveyDto.SurveySaveRequest.builder()
                .level(DifficultyLevel.BEGINNER)
                .interests("daily expressions")
                .rawResponses(raw)
                .build();
    }

    @Nested
    class Save {

        @Test
        @DisplayName("신규 설문 저장: raw + level/interests가 JSON에 포함되어 저장된다")
        void save_new_success() throws Exception {
            // given
            long userId = 1L;
            User user = mkUser(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(surveyRepository.findByUser_Id(userId)).willReturn(Optional.empty());

            // save 시 DB가 반환할 엔티티 모킹
            ArgumentCaptor<Survey> toSave = ArgumentCaptor.forClass(Survey.class);
            given(surveyRepository.save(toSave.capture())).willAnswer(inv -> {
                Survey in = toSave.getValue();
                // id/createdAt 을 채워서 반환(엔티티 @Builder 사용 가정)
                return Survey.builder()
                        .id(99L)
                        .user(in.getUser())
                        .responses(in.getResponses())
                        .build();
            });

            var req = mkReq();

            // when
            var res = sut.save(userId, req);

            // then
            assertThat(res.getSurveyId()).isEqualTo(99L);
            assertThat(res.getCompleted()).isTrue();

            // 저장된 JSON 형식 검증(예외 X)
            String savedJson = toSave.getValue().getResponses();
            assertThatCode(() -> objectMapper.readTree(savedJson))
                    .as("JSON should be parseable")
                    .doesNotThrowAnyException();

            // 내용 검증: Map<String,Object> 로 파싱 후 키/값 확인
            Map<String, Object> parsed = objectMapper.readValue(
                    savedJson, new TypeReference<Map<String, Object>>() {}
            );
            assertThat(parsed)
                    .containsEntry("Which level suits you best?", "BEGINNER")
                    .containsEntry("What kind of words are you into recently?", "daily expressions")
                    .containsEntry("level", "BEGINNER")
                    .containsEntry("interests", "daily expressions");

            // repo save 호출 1회 확인
            verify(surveyRepository, times(1)).save(any(Survey.class));
        }

        @Test
        @DisplayName("기존 설문이 있으면 저장하지 않고 기존 정보 반환")
        void save_existing_returnsExisting() {
            // given
            long userId = 2L;
            User user = mkUser(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Survey existing = Survey.builder()
                    .id(777L)
                    .user(user)
                    .responses("{\"level\":\"BEGINNER\"}")
                    .build();
            // createdAt 은 베이스 엔티티에서 자동이지만, 검증 느슨하게 갈게
            given(surveyRepository.findByUser_Id(userId)).willReturn(Optional.of(existing));

            var req = mkReq();

            // when
            var res = sut.save(userId, req);

            // then
            assertThat(res.getSurveyId()).isEqualTo(777L);
            assertThat(res.getCompleted()).isTrue();
            // 저장 시도 안 함
            verify(surveyRepository, never()).save(any(Survey.class));
        }

        @Test
        @DisplayName("유저가 없으면 예외")
        void save_userNotFound_throws() {
            // given
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> sut.save(123L, mkReq()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("요청 값 누락이면 예외(level/interests 필수)")
        void save_invalidRequest_throws() {
            // given
            long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(mkUser(userId)));

            var bad = SurveyDto.SurveySaveRequest.builder()
                    .level(null)                 // 누락
                    .interests("   ")            // 공백
                    .rawResponses(null)
                    .build();

            // when/then
            assertThatThrownBy(() -> sut.save(userId, bad))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("설문 필드 'level'과 'interests'는 비어 있을 수 없습니다");
        }
    }

    @Nested
    class IsCompleted {

        @Test
        @DisplayName("설문 여부 확인 : true")
        void completed_true() {
            given(surveyRepository.existsByUser_Id(1L)).willReturn(true);
            assertThat(sut.isCompleted(1L)).isTrue();
        }

        @Test
        @DisplayName("설문 여부 확인 : false")
        void completed_false() {
            given(surveyRepository.existsByUser_Id(1L)).willReturn(false);
            assertThat(sut.isCompleted(1L)).isFalse();
        }
    }
}