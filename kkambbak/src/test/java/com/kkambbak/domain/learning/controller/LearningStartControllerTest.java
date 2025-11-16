package com.kkambbak.domain.learning.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.learning.dto.LearningStartDto;
import com.kkambbak.domain.learning.facade.LearningFacade;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LearningStartControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private LearningFacade learningFacade;

    @Test
    void startLearning_AllMode_Test() throws Exception {

        var firstVocab = LearningStartDto.StartResponse.FirstVocabulary.builder()
                .vocabularyId(1L)
                .korean("사과")
                .romanization("sa-gwa")
                .english("apple")
                .imageUrl("https://cdn/image.png")
                .build();

        var mockResponse = LearningStartDto.StartResponse.builder()
                .sessionId(5L)
                .sessionTitle("Topik 1")
                .resultId(103L)
                .vocabIds(List.of(1L, 2L, 3L, 4L, 5L))
                .totalVocabularyCount(5)
                .baseResultId(null)
                .firstVocabulary(firstVocab)
                .build();

        given(learningFacade.startLearning(anyLong(), anyLong(), any()))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/learning/sessions/{sessionId}/start", 5L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType("application/json")
                        .content(toJson(Map.of("mode", "ALL"))))
                .andExpect(status().isOk())
                .andDo(document(
                        "learning-start-all",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 시작")
                                        .description("""
                                        선택한 세션 학습을 시작하고 이번 학습에 사용할 단어 목록 및 첫 단어 정보를 반환합니다.
                                
                                        mode
                                        - ALL (기본값) : 세션에 포함된 모든 단어 학습
                                        - WRONG_ONLY : 이전 학습에서 틀린 단어만 다시 학습 (이전 학습 결과 ID인 baseResultId 필수)
                                        """)
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("sessionId").description("학습을 시작할 세션 ID")
                                        )
                                        .requestFields(
                                                fieldWithPath("mode")

                                                        .optional()
                                                        .description("학습 모드 (ALL / WRONG_ONLY). 기본값은 ALL")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("상태 메시지"),
                                                fieldWithPath("status.description").optional().description("추가 상태 설명"),
                                                fieldWithPath("body.sessionId").description("세션 ID"),
                                                fieldWithPath("body.sessionTitle").description("세션 제목 (예: 'Topik 1')"),
                                                fieldWithPath("body.resultId").description("이번 학습 결과 ID"),
                                                fieldWithPath("body.vocabIds").description("이번 학습에 사용될 단어 ID 목록"),
                                                fieldWithPath("body.totalVocabularyCount").description("전체 학습 단어 수"),
                                                fieldWithPath("body.baseResultId").optional().description("WRONG_ONLY 모드인 경우 기준이 되는 이전 결과 ID, ALL 모드이면 null"),
                                                fieldWithPath("body.firstVocabulary.vocabularyId").description("첫 학습 단어 ID"),
                                                fieldWithPath("body.firstVocabulary.korean").description("첫 학습 단어 (한국어)"),
                                                fieldWithPath("body.firstVocabulary.romanization").description("첫 학습 단어 로마자 표기"),
                                                fieldWithPath("body.firstVocabulary.english").description("첫 학습 단어 영어 뜻"),
                                                fieldWithPath("body.firstVocabulary.imageUrl").description("첫 학습 단어 이미지 URL")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void startLearning_WrongOnlyMode_Test() throws Exception {

        var firstVocab = LearningStartDto.StartResponse.FirstVocabulary.builder()
                .vocabularyId(10L)
                .korean("학교")
                .romanization("hak-gyo")
                .english("school")
                .imageUrl("https://cdn/image-school.png")
                .build();

        var mockResponse = LearningStartDto.StartResponse.builder()
                .sessionId(5L)
                .sessionTitle("Topik 1")
                .resultId(201L)               // 새로운 학습 결과 ID
                .vocabIds(List.of(10L, 11L))  // 이전에 틀렸던 단어들만
                .totalVocabularyCount(2)
                .baseResultId(103L)           // 기준이 되는 이전 학습 결과
                .firstVocabulary(firstVocab)
                .build();

        given(learningFacade.startLearning(anyLong(), anyLong(), any()))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/learning/sessions/{sessionId}/start", 5L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "mode", "WRONG_ONLY",
                                "baseResultId", 103L
                        ))))
                .andExpect(status().isOk())
                .andDo(document(
                        "learning-start-wrong-only",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 시작 (WRONG_ONLY 모드)")
                                        .description("""
                                                이전 학습 결과를 기준으로, 그때 틀렸던 단어만 다시 학습을 시작합니다.

                                                mode:
                                                - WRONG_ONLY: 꼭 지정해야 하며, baseResultId(기준이 되는 이전 학습 결과 ID)를 함께 전달해야 합니다.
                                                """)
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("sessionId").description("학습을 시작할 세션 ID")
                                        )
                                        .requestFields(
                                                fieldWithPath("mode")
                                                        .description("WRONG_ONLY 로 고정 (이전 학습에서 틀린 단어만 다시 학습)"),
                                                fieldWithPath("baseResultId")
                                                        .description("이전 학습 결과 ID. 해당 결과에서 틀린 단어들만 이번 학습에 사용됩니다.")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("상태 메시지"),
                                                fieldWithPath("status.description").optional().description("추가 상태 설명"),
                                                fieldWithPath("body.sessionId").description("세션 ID"),
                                                fieldWithPath("body.sessionTitle").description("세션 제목 (예: 'Topik 1')"),
                                                fieldWithPath("body.resultId").description("이번 학습 결과 ID"),
                                                fieldWithPath("body.vocabIds").description("이번 학습에 사용될 단어 ID 목록 (이전 학습에서 틀린 단어들만 포함)"),
                                                fieldWithPath("body.totalVocabularyCount").description("이번에 다시 풀어야 하는 단어 수"),
                                                fieldWithPath("body.baseResultId").description("기준이 된 이전 학습 결과 ID"),
                                                fieldWithPath("body.firstVocabulary.vocabularyId").description("첫 학습 단어 ID"),
                                                fieldWithPath("body.firstVocabulary.korean").description("첫 학습 단어 (한국어)"),
                                                fieldWithPath("body.firstVocabulary.romanization").description("첫 학습 단어 로마자 표기"),
                                                fieldWithPath("body.firstVocabulary.english").description("첫 학습 단어 영어 뜻"),
                                                fieldWithPath("body.firstVocabulary.imageUrl").description("첫 학습 단어 이미지 URL")
                                        )
                                        .build()
                        )
                ));
    }
}