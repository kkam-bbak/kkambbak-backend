package com.kkambbak.domain.survey.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.CategoryType;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import com.kkambbak.domain.survey.facade.SurveyFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class SurveyControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private SurveyFacade surveyFacade;

    @MockitoBean
    private UserRepository userRepository;

    private SurveyDto.SurveySaveResponse.Priorities topikPri(DifficultyLevel level, List<String> topics) {
        return SurveyDto.SurveySaveResponse.Priorities.builder()
                .categoryType(CategoryType.TOPIK)
                .level(level)
                .topics(topics)
                .build();
    }

    @Test
    @DisplayName("설문 저장 – TOPIK 우선 노출 (문서)")
    void save_topik_docs() throws Exception {
        // Given: 인증은 KkambbakDocumentApiTester가 이미 세팅 (Authorization 헤더도 자동 주입됨 아님)
        long userId = 1L;
        User mockUser = Mockito.mock(User.class);
        given(mockUser.getId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        var resp = SurveyDto.SurveySaveResponse.builder()
                .completed(true)
                .priorities(topikPri(DifficultyLevel.BEGINNER, List.of("topik_1","topik_2")))
                .build();
        given(surveyFacade.saveAndPrioritize(eq(1L), any(SurveyDto.SurveySaveRequest.class)))
                .willReturn(resp);

        this.mockMvc.perform(
                        post("/api/v1/surveys")
                                .contentType("application/json")
                                .content(toJson(
                                        SurveyDto.SurveySaveRequest.builder()
                                                .level(DifficultyLevel.BEGINNER)
                                                .interests("daily expressions")
                                                // rawResponses는 옵션: 필요시 전체 5문항 넣어서 저장 가능
                                                // .rawResponses(Map.of(...))
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andDo(document("survey-save-topik",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Survey")
                                .summary("설문 저장 (TOPIK 우선 노출)")
                                .description("""
                                        설문을 저장하고, 저장 직후 기본 탭(TOPIK)에 맞춘 우선 노출 토픽을 반환합니다.
                                        level은 BEGINNER/INTERMEDIATE/ADVANCED 중 하나, interests는 'daily expressions' 등 단일 선택값입니다.
                                        """)
                                .requestFields(
                                        fieldWithPath("level").type(JsonFieldType.STRING).description("난이도: BEGINNER/INTERMEDIATE/ADVANCED"),
                                        fieldWithPath("interests").type(JsonFieldType.STRING).description("관심사(예: daily expressions)"),
                                        fieldWithPath("rawResponses").type(JsonFieldType.OBJECT)
                                                .optional().description("전체 5문항 원본 응답(JSON). 없으면 level/interests만 저장")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상세 설명"),
                                        fieldWithPath("body.completed").type(JsonFieldType.BOOLEAN).description("설문 저장 완료 여부"),
                                        fieldWithPath("body.priorities.categoryType").type(JsonFieldType.STRING).description("카테고리: TOPIK"),
                                        fieldWithPath("body.priorities.level").type(JsonFieldType.STRING).description("난이도"),
                                        fieldWithPath("body.priorities.topics").type(JsonFieldType.ARRAY).description("우선 노출 토픽 리스트")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("설문 저장 – CASUAL 예시 문서(응답만 목킹)")
    void save_casual_docs() throws Exception {
        long userId = 1L;
        User mockUser = Mockito.mock(User.class);
        given(mockUser.getId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        var resp = SurveyDto.SurveySaveResponse.builder()
                .completed(true)
                .priorities(
                        SurveyDto.SurveySaveResponse.Priorities.builder()
                                .categoryType(CategoryType.CASUAL)
                                .level(null)
                                .topics(List.of("emotions","places"))
                                .build()
                )
                .build();
        given(surveyFacade.saveAndPrioritize(eq(1L), any(SurveyDto.SurveySaveRequest.class)))
                .willReturn(resp);

        this.mockMvc.perform(
                        post("/api/v1/surveys")
                                .contentType("application/json")
                                .content(toJson(
                                        SurveyDto.SurveySaveRequest.builder()
                                                .level(DifficultyLevel.INTERMEDIATE) // CASUAL에선 무시되지만 저장은 됨
                                                .interests("daily expressions")
                                                .build()
                                ))
                )
                .andExpect(status().isOk())
                .andDo(document("survey-save-casual",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Survey")
                                .summary("설문 저장 (CASUAL 응답 예시)")
                                .description("설문을 저장했지만, 프론트에서 CASUAL 탭을 열면 interests에 따른 우선 노출 리스트(emotions, places 등)를 사용합니다.")
                                .requestFields(
                                        fieldWithPath("level").type(JsonFieldType.STRING).description("난이도(저장용)"),
                                        fieldWithPath("interests").type(JsonFieldType.STRING).description("관심사(예: daily expressions)"),
                                        fieldWithPath("rawResponses").type(JsonFieldType.OBJECT).optional().description("전체 5문항 원본 응답(JSON)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상세 설명"),
                                        fieldWithPath("body.completed").type(JsonFieldType.BOOLEAN).description("설문 저장 완료 여부"),
                                        fieldWithPath("body.priorities.categoryType").type(JsonFieldType.STRING).description("카테고리: CASUAL"),
                                        fieldWithPath("body.priorities.level").type(JsonFieldType.STRING).optional().description("CASUAL에서는 null"),
                                        fieldWithPath("body.priorities.topics").type(JsonFieldType.ARRAY).description("우선 노출 토픽 리스트")
                                )
                                .build()
                        )
                ));
    }

    /** 로그인 사용자: 설문 완료(true) */
    @Test
    @DisplayName("GET /api/v1/surveys/check | 로그인 | completed=true")
    void check_completed_true_docs() throws Exception {
        // KkambbakDocumentApiTester 가 SecurityContext에 userId=1L 세팅해둠
        long userId = 1L;
        given(surveyFacade.isCompleted(eq(userId))).willReturn(true);

        this.mockMvc.perform(
                        get("/api/v1/surveys/check")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.completed").value(true))
                .andDo(document("survey-check-completed-true",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Survey")
                                .summary("설문 완료 여부 조회 (로그인)")
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                        fieldWithPath("body.completed").type(JsonFieldType.BOOLEAN).description("설문 완료 여부")
                                )
                                .build()
                        )));
    }

    /** 로그인 사용자: 설문 미완료(false) */
    @Test
    @DisplayName("GET /api/v1/surveys/check | 로그인 | completed=false")
    void check_completed_false_docs() throws Exception {
        long userId = 1L;
        given(surveyFacade.isCompleted(eq(userId))).willReturn(false);

        this.mockMvc.perform(
                        get("/api/v1/surveys/check")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.completed").value(false))
                .andDo(document("survey-check-completed-false",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Survey")
                                .summary("설문 미완료 여부 조회 (로그인)")
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                        fieldWithPath("body.completed").type(JsonFieldType.BOOLEAN).description("설문 완료 여부(false)")
                                )
                                .build()
                        )));
    }

    /** 비로그인: 무조건 completed=false 반환 (컨트롤러 빠른 경로) */
    @Test
    @DisplayName("GET /api/v1/surveys/check | 비로그인 | completed=false(빠른 경로)")
    void check_completed_false_when_unauthenticated_docs() throws Exception {
        // 베이스 클래스가 만든 인증을 비활성화하려면: 헤더를 아예 보내지 않음 + SecurityContext는 베이스 setUp에서 세팅되므로
        // 이 엔드포인트는 컨트롤러에서 @AuthenticationPrincipal==null일 때 false 리턴하는 빠른 경로를 사용.
        // (만약 베이스가 항상 SecurityContext를 채워 넣는다면, 여기에서 SecurityContextHolder.clearContext() 호출 추가)
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        this.mockMvc.perform(get("/api/v1/surveys/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.completed").value(false))
                .andDo(document("survey-check-unauthenticated",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Survey")
                                .summary("설문 완료 여부 조회 (비로그인)")
                                .description("비로그인 상태면 서비스 계층 호출 없이 completed=false를 반환")
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                        fieldWithPath("body.completed").type(JsonFieldType.BOOLEAN).description("항상 false")
                                )
                                .build()
                        )));
    }
}