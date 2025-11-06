package com.kkambbak.domain.learning.controller;

import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import com.kkambbak.domain.learning.dto.LearningSessionListResponse;
import com.kkambbak.domain.learning.dto.SessionCardDto;
import com.kkambbak.domain.learning.service.LearningListService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

class LearningListControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private LearningListService learningListService;

    private SessionCardDto card(long id, String title) {
        return SessionCardDto.builder()
                .id(id)
                .title(title)
                .categoryName("TOPIK")
                .vocabularyCount(3)
                .completed(false)
                .durationSeconds(120)
                .build();
    }

    @Test
    @DisplayName("학습 목록 조회 — 첫 페이지(상위 노출 포함), hasNext=true")
    void getLearningList_firstPage_docs() throws Exception {
        var sessions = List.of(
                card(3, "Topik 3"),
                card(4, "Topik 4"),
                card(1, "Topik 1"),
                card(2, "Topik 2")
        );
        var resp = LearningSessionListResponse.of(CategoryType.TOPIK, sessions, 2L, true);

        given(learningListService.getLearningList(
                eq(1L),                     // KkambbakDocumentApiTester 가짜 유저 ID = 1L
                eq(CategoryType.TOPIK),
                eq("INTERMEDIATE"),
                isNull(),                   // cursor 미지정 → 첫 페이지
                eq(4)
        )).willReturn(resp);

        mockMvc.perform(get("/api/v1/learning/sessions")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("category", "TOPIK")
                        .param("surveyKey", "INTERMEDIATE")
                        .param("limit", "4")
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "learning-list-first-page",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(builder()
                                .tag("Learning")
                                .summary("학습 목록 조회 - 첫 페이지")
                                .description("""
                                카테고리/설문키 기반 학습 세션 목록을 조회합니다.
                                첫 페이지에서 surveyKey(예: BEGINNER, INTERMEDIATE)가 전달되면
                                상위 노출 규칙이 먼저 적용된 뒤 기본 목록이 이어집니다.
                                커서 기반 무한 스크롤을 지원합니다.
                                """)
                                .requestHeaders(headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰"))
                                .queryParameters(
                                        parameterWithName("category").description("카테고리 (TOPIK | CASUAL)"),
                                        parameterWithName("surveyKey").optional().description("설문 키"),
                                        parameterWithName("cursor").optional().description("다음 페이지 커서 (null이면 첫 페이지)"),
                                        parameterWithName("limit").optional().description("페이지 크기 (기본 10, 최대 50)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드 (예: C000=success)"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").optional().description("추가 설명"),
                                        fieldWithPath("body.categoryName").description("카테고리명"),
                                        fieldWithPath("body.sessions[].id").description("세션 ID"),
                                        fieldWithPath("body.sessions[].title").description("세션명"),
                                        fieldWithPath("body.sessions[].categoryName").description("카테고리명"),
                                        fieldWithPath("body.sessions[].vocabularyCount").description("단어 수"),
                                        fieldWithPath("body.sessions[].completed").description("완료 여부"),
                                        fieldWithPath("body.sessions[].durationSeconds").description("소요 시간(초)"),
                                        fieldWithPath("body.nextCursor").description("다음 페이지 커서"),
                                        fieldWithPath("body.hasNext").description("다음 페이지 존재 여부")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("학습 목록 조회 — 다음 페이지(커서 사용), hasNext=false")
    void getLearningList_nextPage_docs() throws Exception {
        // 포스트맨 예시(다음 페이지) 그대로 구성
        var sessions = List.of(
                card(5, "Topik 5"),
                card(6, "Topik 6")
        );
        var resp = LearningSessionListResponse.of(CategoryType.TOPIK, sessions, 6L, false);

        given(learningListService.getLearningList(
                eq(1L),
                eq(CategoryType.TOPIK),
                eq("INTERMEDIATE"),
                eq(2L),
                eq(4)
        )).willReturn(resp);

        mockMvc.perform(get("/api/v1/learning/sessions")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("category", "TOPIK")
                        .param("surveyKey", "INTERMEDIATE")
                        .param("cursor", "2")
                        .param("limit", "4")
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "learning-list-next-page",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(builder()
                                .tag("Learning")
                                .summary("학습 목록 조회 - 다음 페이지")
                                .description("""
                                이전 응답의 nextCursor를 cursor로 넘겨 다음 페이지를 조회합니다.
                                hasNext=false이면 더 이상 페이지가 없습니다.
                                """)
                                .requestHeaders(headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰"))
                                .queryParameters(
                                        parameterWithName("category").description("카테고리 (TOPIK | CASUAL)"),
                                        parameterWithName("surveyKey").optional().description("설문 키"),
                                        parameterWithName("cursor").description("이전 응답의 nextCursor"),
                                        parameterWithName("limit").optional().description("페이지 크기 (기본 10, 최대 50)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").optional().description("추가 설명"),
                                        fieldWithPath("body.categoryName").description("카테고리명"),
                                        fieldWithPath("body.sessions[].id").description("세션 ID"),
                                        fieldWithPath("body.sessions[].title").description("세션명"),
                                        fieldWithPath("body.sessions[].categoryName").description("카테고리명"),
                                        fieldWithPath("body.sessions[].vocabularyCount").description("단어 수"),
                                        fieldWithPath("body.sessions[].completed").description("완료 여부"),
                                        fieldWithPath("body.sessions[].durationSeconds").description("소요 시간(초)"),
                                        fieldWithPath("body.nextCursor").description("다음 페이지 커서"),
                                        fieldWithPath("body.hasNext").description("다음 페이지 존재 여부")
                                )
                                .build())
                ));
    }
}