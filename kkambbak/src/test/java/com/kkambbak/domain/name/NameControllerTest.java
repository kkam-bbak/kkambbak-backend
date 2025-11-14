package com.kkambbak.domain.name;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.name.dto.NameCandidateItemDto;
import com.kkambbak.domain.name.dto.NameResponseDto;
import com.kkambbak.domain.name.dto.NameSelectRequestDto;
import com.kkambbak.domain.name.facade.NameFacade;
import com.kkambbak.domain.name.service.NameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NameControllerTest extends KkambbakDocumentApiTester {
    @MockitoBean
    private NameFacade nameFacade;

    @MockitoBean
    private NameService nameService;


    @Test
    @DisplayName("한국어 이름 생성 API")
    void generateKoreanName() throws Exception {

        NameResponseDto mockResponse =
                NameResponseDto.builder()
                        .historyId(1L)
                        .generationOutput(
                                NameResponseDto.GenerationOutput.builder()
                                        .names(
                                                List.of(
                                                        new NameCandidateItemDto("김다빛", "Kim Da Bit", "A warm gentle light"),
                                                        new NameCandidateItemDto("박서윤", "Park Seo Yoon", "A quiet breeze of hope")
                                                )
                                        )
                                        .build()
                        )
                        .build();

        given(nameFacade.generate(anyLong())).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/name/generate")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("name-generate",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Name")
                                .summary("한국어 이름 생성")
                                .description("""
                                        사용자의 성별,성격 및 이미지, 원하는 이름 느낌을 기반으로
                                        GPT가 한국어 이름 후보 2개를 생성합니다.
                                        """)
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("응답 상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("응답 메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),

                                        fieldWithPath("body.historyId").type(JsonFieldType.NUMBER).description("생성 기록 ID"),
                                        fieldWithPath("body.generationOutput.names").type(JsonFieldType.ARRAY).description("생성된 이름 후보 2개"),
                                        fieldWithPath("body.generationOutput.names[].koreanName").type(JsonFieldType.STRING).description("한국어 이름"),
                                        fieldWithPath("body.generationOutput.names[].romanization").type(JsonFieldType.STRING).description("로마자 표기"),
                                        fieldWithPath("body.generationOutput.names[].poeticMeaning").type(JsonFieldType.STRING).description("이름의 감성적 의미")
                                )
                                .build()
                        )
                ));
    }



    @Test
    @DisplayName("한국어 이름 선택 API")
    void selectKoreanName() throws Exception {

        NameSelectRequestDto request = new NameSelectRequestDto();
        request.setHistoryId(1L);
        request.setKoreanName("김다빛");
        request.setMeaningOfName("A warm gentle light");

        willDoNothing().given(nameFacade)
                .select(eq(10L), any(NameSelectRequestDto.class));

        mockMvc.perform(post("/api/v1/name/select")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("name-select",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Name")
                                .summary("한국어 이름 선택")
                                .description("""
                                        생성된 이름 후보 중 하나를 선택하여
                                        사용자 프로필에 한국어 이름을 최종 저장합니다.
                                        """)
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer 액세스 토큰")
                                )
                                .requestFields(
                                        fieldWithPath("historyId").type(JsonFieldType.NUMBER).description("이름 후보 히스토리 ID"),
                                        fieldWithPath("koreanName").type(JsonFieldType.STRING).description("선택한 한국어 이름"),
                                        fieldWithPath("meaningOfName").type(JsonFieldType.STRING).optional().description("사용자가 직접 지정한 이름 의미")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("응답 상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("메시지"),
                                        fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명")
                                )
                                .build()
                        )
                ));
    }
}
