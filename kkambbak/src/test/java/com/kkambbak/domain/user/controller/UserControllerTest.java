package com.kkambbak.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends KkambbakDocumentApiTester {

    @Test
    void testLoginTest() throws Exception {
        // given
        LoginTokenDto.Response mockResponse = LoginTokenDto.Response.builder()
                .userId(1L)
                .email("test@example.com")
                .tokenData(TokenDataDto.builder()
                        .grantType("Bearer")
                        .accessToken("mockAccessToken")
                        .refreshToken("mockRefreshToken")
                        .accessTokenExpiredAt(System.currentTimeMillis() + 3600000)
                        .refreshTokenExpiredAt(System.currentTimeMillis() + 86400000)
                        .build())
                .build();
        given(authService.testLoginByEmail(anyString(), anyString())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/users/test-login")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "email", "test@example.com",
                                "key", "a9F3kLmP7wQzX1bC"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-test-login",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("테스트 로그인")
                                        .description("이메일과 인증 키로 테스트용 액세스 토큰과 리프레시 토큰을 발급받습니다")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("key").type(JsonFieldType.STRING).description("인증 키")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                                fieldWithPath("body.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("body.tokenData").type(JsonFieldType.OBJECT).description("토큰 정보"),
                                                fieldWithPath("body.tokenData.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.tokenData.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                                fieldWithPath("body.tokenData.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                                fieldWithPath("body.tokenData.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (timestamp)"),
                                                fieldWithPath("body.tokenData.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (timestamp)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void refreshTokenTest() throws Exception {
        // given
        TokenDataDto mockTokenData = TokenDataDto.builder()
                .grantType("Bearer")
                .accessToken("newMockAccessToken")
                .refreshToken("newMockRefreshToken")
                .accessTokenExpiredAt(System.currentTimeMillis() + 3600000)
                .refreshTokenExpiredAt(System.currentTimeMillis() + 86400000)
                .build();
        given(authService.refreshToken(anyString())).willReturn(mockTokenData);

        // when & then
        this.mockMvc.perform(post("/api/v1/users/refresh")
                        .header("RefreshToken", "refresh_token_example"))
                .andExpect(status().isOk())
                .andDo(document("user-refresh-token",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("토큰 리프레시")
                                        .description("리프레시 토큰으로 액세스 토큰 갱신")
                                        .requestHeaders(
                                                headerWithName("RefreshToken").description("리프레시 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("토큰 정보"),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("새로운 액세스 토큰"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("새로운 리프레시 토큰"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (timestamp)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (timestamp)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void logoutTest() throws Exception {
        // given
        doNothing().when(userService).logout(any());

        // when & then
        this.mockMvc.perform(post("/api/v1/users/logout")
                        .header("Authorization", "Bearer access_token_example"))
                .andExpect(status().isOk())
                .andDo(document("user-logout",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("로그아웃")
                                        .description("현재 액세스 토큰을 블랙리스트에 추가하여 로그아웃")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.NULL).description("응답 본문 (null)").optional()
                                        )
                                        .build()
                        )
                ));
    }
}