package com.kkambbak.domain.learning.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.learning.enums.LearningMode;
import com.kkambbak.domain.learning.dto.LearningStartDto;
import com.kkambbak.domain.learning.service.LearningStartService;
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
    private LearningStartService learningStartService;

    @Test
    void startLearning_AllMode_Test() throws Exception {
        // given
        LearningStartDto.StartResponse.FirstVocabulary firstVocab =
                LearningStartDto.StartResponse.FirstVocabulary.builder()
                        .vocabularyId(1L)
                        .korean("사과")
                        .romanization("sa-gwa")
                        .english("apple")
                        .imageUrl("https://pub-xxxx.r2.dev/images/apple.png")
                        .build();

        LearningStartDto.StartResponse mockResponse =
                LearningStartDto.StartResponse.builder()
                        .sessionId(5L)
                        .resultId(103L)
                        .vocabIds(List.of(1L, 2L, 3L, 4L, 5L))
                        .totalVocabularyCount(5)
                        .baseResultId(null)
                        .firstVocabulary(firstVocab)
                        .build();

        given(learningStartService.start(anyLong(), anyLong(), any()))
                .willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/learning/sessions/{sessionId}/start", 5L)
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "mode", "ALL"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("learning-start-all",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 시작 - 전체 학습 (ALL 모드)")
                                        .description("""
                                        세션 학습을 시작하고 새로운 학습 결과(LearningResult)를 생성합니다.
                                        #### 1. 전체 학습 (ALL)
                                        - 세션의 모든 단어를 학습합니다.
                                        - mode를 생략하거나 "ALL"로 지정하면 됩니다.
                                        - baseResultId는 null입니다.
                                        - 세션에 포함된 모든 단어가 vocabIds에 포함됩니다.
                                        
                                        #### 2. 오답만 학습 (WRONG_ONLY)
                                        - 이전 학습에서 틀린 단어만 다시 학습합니다.
                                        - mode를 "WRONG_ONLY"로 지정해야 합니다.
                                        - **baseResultId 필수**: 기준이 되는 이전 학습 결과 ID를 지정합니다.
                                        - 해당 결과에서 틀린 단어만 vocabIds에 포함됩니다.
                                        - 응답의 baseResultId에 기준 결과 ID가 포함됩니다.
                                        """)
                                        .requestHeaders(
                                                headerWithName("Authorization")
                                                        .description("Bearer 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("sessionId")
                                                        .description("학습할 세션 ID")
                                        )
                                        .requestFields(
                                                fieldWithPath("mode")
                                                        .type(JsonFieldType.STRING)
                                                        .description("학습 모드\n\n" +
                                                                "- `ALL`: 전체 학습 (기본값)\n" +
                                                                "- `WRONG_ONLY`: 오답만 학습")
                                                        .optional(),
                                                fieldWithPath("baseResultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("오답 재학습 시 기준이 되는 결과 ID\n\n" +
                                                                "**WRONG_ONLY 모드일 때만 필수**")
                                                        .optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 코드"),
                                                fieldWithPath("status.message")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 메시지"),
                                                fieldWithPath("status.description")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 설명")
                                                        .optional(),
                                                fieldWithPath("body")
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("응답 데이터"),
                                                fieldWithPath("body.sessionId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("학습 중인 세션 ID"),
                                                fieldWithPath("body.resultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("새로 생성된 학습 결과 ID\n\n" +
                                                                "Grade/Complete API 호출 시 사용"),
                                                fieldWithPath("body.vocabIds")
                                                        .type(JsonFieldType.ARRAY)
                                                        .description("학습할 단어 ID 목록\n\n" +
                                                                "- ALL 모드: 세션의 모든 단어\n" +
                                                                "- WRONG_ONLY: 틀린 단어만\n" +
                                                                "- 세션 순서대로 정렬"),
                                                fieldWithPath("body.totalVocabularyCount")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("총 학습할 단어 개수"),
                                                fieldWithPath("body.baseResultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("오답 재학습의 기준이 된 결과 ID\n\n" +
                                                                "- ALL 모드: `null`\n" +
                                                                "- WRONG_ONLY: 기준 결과 ID")
                                                        .optional(),
                                                fieldWithPath("body.firstVocabulary")
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("첫 번째 단어 정보\n\n" +
                                                                "바로 화면에 표시할 수 있도록 상세 정보 포함"),
                                                fieldWithPath("body.firstVocabulary.vocabularyId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("단어 ID"),
                                                fieldWithPath("body.firstVocabulary.korean")
                                                        .type(JsonFieldType.STRING)
                                                        .description("한국어 단어"),
                                                fieldWithPath("body.firstVocabulary.romanization")
                                                        .type(JsonFieldType.STRING)
                                                        .description("로마자 표기 (발음 참고용)"),
                                                fieldWithPath("body.firstVocabulary.english")
                                                        .type(JsonFieldType.STRING)
                                                        .description("영어 뜻"),
                                                fieldWithPath("body.firstVocabulary.imageUrl")
                                                        .type(JsonFieldType.STRING)
                                                        .description("이미지 URL")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void startLearning_WrongOnlyMode_Test() throws Exception {
        // given
        LearningStartDto.StartResponse.FirstVocabulary firstVocab =
                LearningStartDto.StartResponse.FirstVocabulary.builder()
                        .vocabularyId(3L)
                        .korean("딸기")
                        .romanization("ttal-gi")
                        .english("strawberry")
                        .imageUrl("https://pub-xxxx.r2.dev/images/strawberry.png")
                        .build();

        LearningStartDto.StartResponse mockResponse =
                LearningStartDto.StartResponse.builder()
                        .sessionId(5L)
                        .resultId(104L)
                        .vocabIds(List.of(3L, 7L, 12L))
                        .totalVocabularyCount(3)
                        .baseResultId(100L)
                        .firstVocabulary(firstVocab)
                        .build();

        given(learningStartService.start(anyLong(), anyLong(), any()))
                .willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/learning/sessions/{sessionId}/start", 5L)
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "mode", "WRONG_ONLY",
                                "baseResultId", 100L
                        ))))
                .andExpect(status().isOk())
                .andDo(document("learning-start-wrong-only",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 시작 - 오답만 학습 (WRONG_ONLY 모드)")
                                        .description("""
                                        ## 학습 시작 API - 오답 재학습
                                        이전 학습에서 틀린 단어만 다시 학습합니다.
                                        
                                        ### 오답만 학습 (WRONG_ONLY) 모드
                                        - 이전 학습 결과를 기준으로 틀린 단어만 추출합니다.
                                        - **baseResultId 필수**: 기준이 되는 학습 결과 ID
                                        - 틀린 단어만 vocabIds에 포함됩니다.
                                        - 세션 순서대로 정렬되어 반환됩니다.
                                        """)
                                        .requestHeaders(
                                                headerWithName("Authorization")
                                                        .description("Bearer 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("sessionId")
                                                        .description("학습할 세션 ID")
                                        )
                                        .requestFields(
                                                fieldWithPath("mode")
                                                        .type(JsonFieldType.STRING)
                                                        .description("`WRONG_ONLY` 고정"),
                                                fieldWithPath("baseResultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("기준이 되는 학습 결과 ID (필수)\n\n" +
                                                                "해당 결과에서 틀린 단어만 추출")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 코드"),
                                                fieldWithPath("status.message")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 메시지"),
                                                fieldWithPath("status.description")
                                                        .type(JsonFieldType.STRING)
                                                        .description("상태 설명")
                                                        .optional(),
                                                fieldWithPath("body")
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("응답 데이터"),
                                                fieldWithPath("body.sessionId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("학습 중인 세션 ID"),
                                                fieldWithPath("body.resultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("새로 생성된 학습 결과 ID"),
                                                fieldWithPath("body.vocabIds")
                                                        .type(JsonFieldType.ARRAY)
                                                        .description("틀린 단어 ID 목록 (세션 순서 유지)"),
                                                fieldWithPath("body.totalVocabularyCount")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("틀린 단어 개수"),
                                                fieldWithPath("body.baseResultId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("기준이 된 학습 결과 ID"),
                                                fieldWithPath("body.firstVocabulary")
                                                        .type(JsonFieldType.OBJECT)
                                                        .description("첫 번째 오답 단어 정보"),
                                                fieldWithPath("body.firstVocabulary.vocabularyId")
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("단어 ID"),
                                                fieldWithPath("body.firstVocabulary.korean")
                                                        .type(JsonFieldType.STRING)
                                                        .description("한국어 단어"),
                                                fieldWithPath("body.firstVocabulary.romanization")
                                                        .type(JsonFieldType.STRING)
                                                        .description("로마자 표기"),
                                                fieldWithPath("body.firstVocabulary.english")
                                                        .type(JsonFieldType.STRING)
                                                        .description("영어 뜻"),
                                                fieldWithPath("body.firstVocabulary.imageUrl")
                                                        .type(JsonFieldType.STRING)
                                                        .description("이미지 URL")
                                        )
                                        .build()
                        )
                ));
    }

}