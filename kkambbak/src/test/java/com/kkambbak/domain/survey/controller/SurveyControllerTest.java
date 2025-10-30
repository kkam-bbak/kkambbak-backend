package com.kkambbak.domain.survey.controller;

import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import com.kkambbak.domain.survey.service.SurveyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

class SurveyControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private SurveyService surveyService;

    @Test
    @DisplayName("설문 저장 성공 — 200 OK (body 없음)")
    void saveSurvey_success_docs() throws Exception {
        String rawJson = """
                {"Why are you learning Korean?": "Preparing for the TOPIK exam",
                 "How do you like to study?": "Quick and focused learning",
                 "Which level suits you best?": "BEGINNER",
                 "How much time do you want to spend per session?": "5mins",
                 "What kind of words are you most interested in?": "daily expressions"}
                """;

        SurveyDto.SurveySaveRequest req = SurveyDto.SurveySaveRequest.builder()
                .level(DifficultyLevel.BEGINNER)
                .interests("daily expressions")
                .rawResponses(rawJson)
                .build();

        // 서비스는 void 메서드
        doNothing().when(surveyService).save(eq(1L), any(SurveyDto.SurveySaveRequest.class));

        // when & then
        mockMvc.perform(
                        post("/api/v1/surveys")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(req))
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "surveys-save-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(builder()
                                .tag("Surveys")
                                .summary("설문 저장")
                                .description("""
                                        사용자의 설문을 최초 1회 저장합니다.
                                        동일 사용자 재요청 시 저장을 생략(중복 방지)하고 성공(status)만 반환합니다.
                                        """)
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .requestFields(
                                        fieldWithPath("level").description("난이도 (BEGINNER | INTERMEDIATE | ADVANCED)"),
                                        fieldWithPath("interests").description("관심사 (예: daily expressions)"),
                                        fieldWithPath("rawResponses").description("원본 설문 응답(JSON 문자열)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드 (예: C000=success)"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").optional().description("추가 설명 (nullable)")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("설문 완료 여부 — completed=true")
    void checkSurvey_completed_true_docs() throws Exception {
        given(surveyService.isCompleted(eq(1L))).willReturn(true);

        mockMvc.perform(
                        get("/api/v1/surveys/check")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "surveys-check-true",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(builder()
                                .tag("Surveys")
                                .summary("설문 완료 여부 확인")
                                .description("해당 사용자가 설문을 완료했는지 여부를 반환합니다.")
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").optional().description("추가 설명 (nullable)"),
                                        fieldWithPath("body.completed").description("설문 완료 여부(true)")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("설문 완료 여부 — completed=false")
    void checkSurvey_completed_false_docs() throws Exception {
        given(surveyService.isCompleted(eq(1L))).willReturn(false);

        mockMvc.perform(
                        get("/api/v1/surveys/check")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "surveys-check-false",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(builder()
                                .tag("Surveys")
                                .summary("설문 완료 여부 확인")
                                .description("해당 사용자가 설문을 완료했는지 여부를 반환합니다.")
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").optional().description("추가 설명 (nullable)"),
                                        fieldWithPath("body.completed").description("설문 완료 여부(false)")
                                )
                                .build())
                ));
    }
}