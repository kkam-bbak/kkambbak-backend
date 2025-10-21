package com.kkambbak.domain.roleplay.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoleplayControllerTest extends KkambbakDocumentApiTester {
    @MockitoBean
    private RoleplayFacade roleplayFacade;

    @Test
    @DisplayName("roleplay scenarios 조회 API 테스트")
    void getAllRolePlayScenarios() throws Exception {
        //given
        List<RoleplayResponseDto> mockList = List.of(
                new RoleplayResponseDto(1L,"At a Cafe","카페에서 쓰이는 표현","BEGINNER",6),
                new RoleplayResponseDto(1L,"At school","학교 생활에서 쓰이는 표현","INTERMEDIATE",4),
                new RoleplayResponseDto(1L,"At Hospital","병원에서 쓰이는 표현","ADVANCED",10)
        );
        given(roleplayFacade.getAllRoleplayScenarios()).willReturn(mockList);

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
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).optional().description("상태 설명"),
                                                fieldWithPath("body").type(JsonFieldType.ARRAY).description("응답 데이터"),
                                                fieldWithPath("body[].id").type(JsonFieldType.NUMBER).description("시나리오 ID"),
                                                fieldWithPath("body[].title").type(JsonFieldType.STRING).description("시나리오 제목"),
                                                fieldWithPath("body[].description").type(JsonFieldType.STRING).description("시나리오 설명"),
                                                fieldWithPath("body[].difficultyLevel").type(JsonFieldType.STRING).description("난이도"),
                                                fieldWithPath("body[].estimated_minutes").type(JsonFieldType.NUMBER).description("예상 소요 시간")
                                        )
                                        .build()
                        )

                ));
    }

}
