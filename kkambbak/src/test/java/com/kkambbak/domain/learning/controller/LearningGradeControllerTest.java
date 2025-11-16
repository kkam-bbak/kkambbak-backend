package com.kkambbak.domain.learning.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.learning.enums.GradeAction;
import com.kkambbak.domain.learning.dto.LearningGradeDto;
import com.kkambbak.domain.learning.facade.LearningFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LearningGradeControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private LearningFacade learningFacade;

    @Test
    @DisplayName("학습 단어 발음 채점 API")
    void gradeLearningTest() throws Exception {

        var mockResponse = LearningGradeDto.GradeResponse.of(
                true,
                true,
                false,
                LearningGradeDto.Next.builder()
                        .itemId(2L)
                        .korean("학교")
                        .romanization("hakgyo")
                        .english("school")
                        .imageUrl("img.png")
                        .build(),
                null
        );

        given(learningFacade.gradeLearning(anyLong(), anyLong(), any(), any()))
                .willReturn(mockResponse);

        MockMultipartFile audioFile = new MockMultipartFile(
                "audioFile",
                "voice.wav",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "dummy audio data".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/learning/{sessionId}/grade", 1L)
                                .file(audioFile)
                                .param("action", GradeAction.GRADE.name())
                                .param("itemId", "1")
                                .header("Authorization", TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andDo(document("learning-grade",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Learning")
                                        .summary("학습 발음 채점")
                                        .description("""
                                        학습 중 특정 단어에 대해 발음 채점을 수행하고, 정답 여부에 따라 다음 학습 흐름을 결정합니다.
                                        multipart/form-data 요청을 사용하며, action 값에 따라 동작이 달라집니다.
                                
                                        요청
                                        - action: GRADE 또는 NEXT_AFTER_WRONG
                                         GRADE → 음성을 기반으로 발음 평가 후 정답 여부 판단
                                         NEXT_AFTER_WRONG → 발음 평가 없이 오답으로 처리하고 다음 단어로 이동
                                        - itemId: 현재 채점 중인 단어(Vocabulary)의 ID
                                        - audioFile: 업로드한 음성 파일 (action=GRADE일 때 필수)
                                
                                        응답
                                        - correct: 이번 단어의 정답 여부 
                                        - moved: 다음 단어로 넘어갈 수 있는지 여부
                                        - finished: 학습 세션이 종료되었는지 여부
                                        - next: 다음 단어 정보 (moved=true & finished=false 때 반환)
                                        - correctAnswer: 오답일 때 정답으로 보여줄 단어 정보 (NEXT_AFTER_WRONG일 때 사용)
                                        """)
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상태 설명").optional(),

                                                fieldWithPath("body.correct").type(JsonFieldType.BOOLEAN).description("현재 문제 정답 여부"),
                                                fieldWithPath("body.moved").type(JsonFieldType.BOOLEAN).description("다음 문제로 이동했는지 여부"),
                                                fieldWithPath("body.finished").type(JsonFieldType.BOOLEAN).description("학습 세션이 종료되었는지 여부"),
                                                fieldWithPath("body.next").optional().type(JsonFieldType.OBJECT).description("다음 학습 단어 정보").optional(),
                                                fieldWithPath("body.next.itemId").optional().type(JsonFieldType.NUMBER).description("다음 단어 ID").optional(),
                                                fieldWithPath("body.next.korean").optional().type(JsonFieldType.STRING).description("다음 단어 (한국어)").optional(),
                                                fieldWithPath("body.next.romanization").optional().type(JsonFieldType.STRING).description("다음 단어 로마자 표기").optional(),
                                                fieldWithPath("body.next.english").optional().type(JsonFieldType.STRING).description("다음 단어 영어").optional(),
                                                fieldWithPath("body.next.imageUrl").optional().type(JsonFieldType.STRING).description("다음 단어 이미지 URL").optional(),

                                                fieldWithPath("body.correctAnswer").optional().type(JsonFieldType.OBJECT).description("오답 시 정답 정보").optional()
                                        )
                                        .build()
                        )
                ));
    }
}