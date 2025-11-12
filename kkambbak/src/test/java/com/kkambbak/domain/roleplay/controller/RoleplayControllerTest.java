package com.kkambbak.domain.roleplay.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.roleplay.enums.PronunciationResult;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import com.kkambbak.domain.roleplay.dto.RoleplayDialoguesResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplayEvaluateResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import com.kkambbak.domain.roleplay.service.RoleplayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoleplayControllerTest extends KkambbakDocumentApiTester {
    @MockitoBean
    private RoleplayFacade roleplayFacade;

    @MockitoBean
    private RoleplayService  roleplayService;

    @Test
    @DisplayName("roleplay scenarios 조회 API")
    void getAllRolePlayScenarios() throws Exception {
        //given
        List<RoleplayResponseDto> mockList = List.of(
                new RoleplayResponseDto(1L,"At a Cafe","카페에서 쓰이는 표현",6),
                new RoleplayResponseDto(2L,"At school","학교 생활에서 쓰이는 표현",4),
                new RoleplayResponseDto(3L,"At Hospital","병원에서 쓰이는 표현",10)
        );
        given(roleplayService.getAllRoleplayScenarios()).willReturn(mockList);

        //when & then
        mockMvc.perform(get("/api/v1/roleplay/all")
                .header(AUTH_HEADER,TEST_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].title").value("At a Cafe"))
                .andExpect(jsonPath("$.body[1].title").value("At school"))
                .andExpect(jsonPath("$.body[2].title").value("At Hospital"))
                .andDo(document("roleplay-get-all",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Roleplay")
                                        .summary("Roleplay Scenarios 리스트 반환")
                                        .description("DB에 저장된 Roleplay Scenarios 리스트가 반환됩니다.")
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                                fieldWithPath("body").type(JsonFieldType.ARRAY).description("응답 데이터"),
                                                fieldWithPath("body[].id").type(JsonFieldType.NUMBER).description("시나리오 ID"),
                                                fieldWithPath("body[].title").type(JsonFieldType.STRING).description("시나리오 제목"),
                                                fieldWithPath("body[].description").type(JsonFieldType.STRING).description("시나리오 설명"),
                                                fieldWithPath("body[].estimated_minutes").type(JsonFieldType.NUMBER).description("예상 소요 시간(분)")
                                        )
                                        .build()
                        )

                ));
    }

    @Test
    @DisplayName("Roleplay 시작 API")
    void startRoleplayScenario() throws Exception {
        var mockResponse = RoleplayDialoguesResponseDto.builder()
                .sessionId(1L)
                .dialogueId(1L)
                .role("staff")
                .speaker(SpeakerType.AI)
                .english("Hello! What would you like to order?")
                .korean("안녕하세요! 무엇을 주문하시겠어요?")
                .romanized("Annyeonghaseyo! Mueoseul jumunhasigesseoyo?")
                .mismatchKorean("나는 졸려요.")
                .mismatchEnglish("I'm sleepy")
                .mismatchRomanized("naneun jollyeoyo.")
                .coreWord("order")
                .build();

        given(roleplayFacade.start(anyLong(), anyLong())).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/roleplay/start?scenarioId=1")
                        .param("scenarioId", "1")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("roleplay-start",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Roleplay")
                                .summary("Roleplay 세션 시작")
                                .description("선택한 시나리오로 Roleplay 세션을 시작하고 AI의 첫 문장를 반환합니다.")
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .queryParameters(
                                        parameterWithName("scenarioId").description("시작할 시나리오 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),

                                        fieldWithPath("body.sessionId").type(JsonFieldType.NUMBER).description("세션 ID"),
                                        fieldWithPath("body.dialogueId").type(JsonFieldType.NUMBER).description("문장 ID"),
                                        fieldWithPath("body.korean").type(JsonFieldType.STRING).description("한국어 문장"),
                                        fieldWithPath("body.romanized").type(JsonFieldType.STRING).description("로마자 표기"),
                                        fieldWithPath("body.english").type(JsonFieldType.STRING).description("영문 문장"),
                                        fieldWithPath("body.speaker").type(JsonFieldType.STRING).description("화자 타입 (AI/USER)"),
                                        fieldWithPath("body.role").type(JsonFieldType.STRING).description("화자 역할"),

                                        fieldWithPath("body.mismatchKorean").type(JsonFieldType.STRING).optional().description("틀린 한국어 문장"),
                                        fieldWithPath("body.mismatchRomanized").type(JsonFieldType.STRING).optional().description("틀린 로마자 표기"),
                                        fieldWithPath("body.mismatchEnglish").type(JsonFieldType.STRING).optional().description("틀린 영문 문장"),

                                        fieldWithPath("body.coreWord").type(JsonFieldType.STRING).optional().description("힌트 핵심 단어(영어)")
                                ).build()
                        )
                ));
    }

    @Test
    @DisplayName("Roleplay 다음 문장 생성 API")
    void nextRoleplayDialogue() throws Exception {
        // given
        var mockResponse = RoleplayDialoguesResponseDto.builder()
                .sessionId(1L)
                .dialogueId(7L)
                .role("customer")
                .speaker(SpeakerType.USER)
                .english("One Americano, please.")
                .korean("아메리카노 한 잔 주세요.")
                .romanized("Amerikano han jan juseyo.")
                .mismatchKorean("졸려요.")
                .mismatchEnglish("I'm sleepy.")
                .mismatchRomanized("naneun jollyeoyo")
                .coreWord("Americano")
                .build();

        given(roleplayFacade.next(anyLong(), anyLong())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/roleplay/next?sessionId=1")
                        .param("sessionId", "1")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("roleplay-next",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Roleplay")
                                .summary("다음 문장 생성")
                                .description("""
                                    현재 세션의 맥락에 맞는 다음 문장을 반환합니다.
                                    speaker가 USER일 경우, mismatch 문장을 카드섹션에 활용합니다.
                                    """)
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .queryParameters(
                                        parameterWithName("sessionId").description("세션 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),

                                        fieldWithPath("body.sessionId").type(JsonFieldType.NUMBER).description("세션 ID"),
                                        fieldWithPath("body.dialogueId").type(JsonFieldType.NUMBER).description("문장 ID"),
                                        fieldWithPath("body.korean").type(JsonFieldType.STRING).description("한국어 문장"),
                                        fieldWithPath("body.romanized").type(JsonFieldType.STRING).description("로마자 표기"),
                                        fieldWithPath("body.english").type(JsonFieldType.STRING).description("영문 문장"),
                                        fieldWithPath("body.speaker").type(JsonFieldType.STRING).description("화자 타입 (AI/USER)"),
                                        fieldWithPath("body.role").type(JsonFieldType.STRING).description("화자 역할"),

                                        fieldWithPath("body.mismatchKorean").type(JsonFieldType.STRING).optional().description("틀린 한국어 문장"),
                                        fieldWithPath("body.mismatchRomanized").type(JsonFieldType.STRING).optional().description("틀린 로마자 표기"),
                                        fieldWithPath("body.mismatchEnglish").type(JsonFieldType.STRING).optional().description("틀린 영문 문장"),

                                        fieldWithPath("body.coreWord").type(JsonFieldType.STRING).optional().description("힌트 핵심 단어(영어)")
                                ).build()
                        )
                ));
    }


    @Test
    @DisplayName("Roleplay 발음 평가 API")
    void evaluatePronunciation() throws Exception {
        // given
        var mockResponse = RoleplayEvaluateResponseDto.builder()
                .dialogueId(2L)
                .score(87.5)
                .feedback(PronunciationResult.GOOD)
                .build();

        given(roleplayFacade.evaluate(anyLong(), anyLong(), anyLong(), any()))
                .willReturn(mockResponse);


        MockMultipartFile audioFile = new MockMultipartFile(
                "audioFile", "sample.wav",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "dummy audio data".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/roleplay/evaluate")
                        .file(audioFile)
                        .param("sessionId", "1")
                        .param("dialogueId", "2")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("roleplay-evaluate",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Roleplay")
                                .summary("발음 평가")
                                .description("""
                                    사용자가 업로드한 음성 파일을 기반으로 발음 평가를 수행합니다.
                                    multipart/form-data 형식으로 요청합니다.
                                    - `audioFile`: 업로드 음성 파일 (WAV 등)
                                    - `sessionId`: 세션 ID
                                    - `dialogueId`: 문장 ID
                                    """)
                                .requestHeaders(
                                        headerWithName("Authorization").description("Bearer 토큰")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                        fieldWithPath("body.dialogueId").type(JsonFieldType.NUMBER).description("문장 ID"),
                                        fieldWithPath("body.score").type(JsonFieldType.NUMBER).description("발음 점수 (0~100 실수형)"),
                                        fieldWithPath("body.feedback").type(JsonFieldType.STRING).description("발음 평가 결과 (GOOD / RETRY / WRONG)")
                                )
                                .build()
                        )
                ));
    }





}
