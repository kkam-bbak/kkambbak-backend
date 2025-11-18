package com.kkambbak.domain.learning.controller;


import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.learning.dto.LearningResultDto;
import com.kkambbak.domain.learning.service.LearningResultQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LearningResultControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private LearningResultQueryService learningResultQueryService;


    @Test
    void getLatestSummaryTest() throws Exception {
        Long sessionId = 1L;
        Long resultId = 10L;

        LearningResultDto.SummaryResponse mockSummary =
                LearningResultDto.SummaryResponse.builder()
                        .sessionId(sessionId)
                        .resultId(resultId)
                        .sessionTitle("Emotions")
                        .totalCount(20)
                        .correctCount(15)
                        .durationSeconds(390L)
                        .completedAt(LocalDateTime.of(2025, 11, 17, 21, 11, 11))
                        .build();

        given(learningResultQueryService.getLatestSummary(anyLong(), anyLong()))
                .willReturn(mockSummary);

        this.mockMvc.perform(
                        get("/api/v1/learning/{sessionId}/results/summary", sessionId)
                                .header("Authorization", "Bearer access_token_example")
                )
                .andExpect(status().isOk())
                .andDo(document("learning-result-summary",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 결과 요약 조회")
                                        .description("해당 세션에 대한 사용자의 학습 결과 요약 정보를 조회합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING)
                                                        .description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING)
                                                        .description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING)
                                                        .description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT)
                                                        .description("응답 데이터"),

                                                fieldWithPath("body.sessionId").type(JsonFieldType.NUMBER)
                                                        .description("세션 ID"),
                                                fieldWithPath("body.resultId").type(JsonFieldType.NUMBER)
                                                        .description("학습 결과 ID"),
                                                fieldWithPath("body.sessionTitle").type(JsonFieldType.STRING)
                                                        .description("세션 제목"),
                                                fieldWithPath("body.totalCount").type(JsonFieldType.NUMBER)
                                                        .description("전체 단어 수"),
                                                fieldWithPath("body.correctCount").type(JsonFieldType.NUMBER)
                                                        .description("정답 개수"),
                                                fieldWithPath("body.durationSeconds").type(JsonFieldType.NUMBER)
                                                        .description("소요 시간 (초 단위)"),
                                                fieldWithPath("body.completedAt").type(JsonFieldType.STRING)
                                                        .description("학습 완료 시각")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getLatestReviewTest() throws Exception {
        // given
        Long sessionId = 1L;
        Long resultId = 10L;

        LearningResultDto.SummaryResponse mockSummary =
                LearningResultDto.SummaryResponse.builder()
                        .sessionId(sessionId)
                        .resultId(resultId)
                        .sessionTitle("Emotions")
                        .totalCount(3)
                        .correctCount(2)
                        .durationSeconds(120L)
                        .completedAt(LocalDateTime.of(2025, 11, 17, 21, 20, 0))
                        .build();

        LearningResultDto.ReviewItem item1 =
                LearningResultDto.ReviewItem.builder()
                        .vocabularyId(101L)
                        .korean("행복하다")
                        .romanization("haengbokhada")
                        .english("happy")
                        .correct(true)
                        .build();

        LearningResultDto.ReviewItem item2 =
                LearningResultDto.ReviewItem.builder()
                        .vocabularyId(102L)
                        .korean("슬프다")
                        .romanization("seulpeuda")
                        .english("sad")
                        .correct(false)
                        .build();

        LearningResultDto.ReviewResponse mockReview =
                LearningResultDto.ReviewResponse.builder()
                        .summary(mockSummary)
                        .items(List.of(item1, item2))
                        .build();

        given(learningResultQueryService.getLatestReview(anyLong(), anyLong()))
                .willReturn(mockReview);

        this.mockMvc.perform(
                        get("/api/v1/learning/{sessionId}/results/review", sessionId)
                                .header("Authorization", "Bearer access_token_example")
                )
                .andExpect(status().isOk())
                .andDo(document("learning-result-review",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 결과 요약 및 정오답 리스트 조회")
                                        .description("해당 세션에 대한 학습 결과 요약 정보 및 단어별 정오답 리스트를 조회합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING)
                                                        .description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING)
                                                        .description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING)
                                                        .description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT)
                                                        .description("응답 데이터"),

                                                fieldWithPath("body.summary").type(JsonFieldType.OBJECT)
                                                        .description("학습 결과 요약"),
                                                fieldWithPath("body.summary.sessionId").type(JsonFieldType.NUMBER)
                                                        .description("세션 ID"),
                                                fieldWithPath("body.summary.resultId").type(JsonFieldType.NUMBER)
                                                        .description("학습 결과 ID"),
                                                fieldWithPath("body.summary.sessionTitle").type(JsonFieldType.STRING)
                                                        .description("세션 제목"),
                                                fieldWithPath("body.summary.totalCount").type(JsonFieldType.NUMBER)
                                                        .description("전체 단어 수"),
                                                fieldWithPath("body.summary.correctCount").type(JsonFieldType.NUMBER)
                                                        .description("정답 개수"),
                                                fieldWithPath("body.summary.durationSeconds").type(JsonFieldType.NUMBER)
                                                        .description("소요 시간 (초 단위)"),
                                                fieldWithPath("body.summary.completedAt").type(JsonFieldType.STRING)
                                                        .description("학습 완료 시각"),

                                                fieldWithPath("body.items").type(JsonFieldType.ARRAY)
                                                        .description("단어별 정오답 리스트"),
                                                fieldWithPath("body.items[].vocabularyId").type(JsonFieldType.NUMBER)
                                                        .description("단어 ID"),
                                                fieldWithPath("body.items[].korean").type(JsonFieldType.STRING)
                                                        .description("한국어 단어"),
                                                fieldWithPath("body.items[].romanization").type(JsonFieldType.STRING)
                                                        .description("로마자 표기"),
                                                fieldWithPath("body.items[].english").type(JsonFieldType.STRING)
                                                        .description("영어 뜻"),
                                                fieldWithPath("body.items[].correct").type(JsonFieldType.BOOLEAN)
                                                        .description("정답 여부")
                                        )
                                        .build()
                        )
                ));
    }
}