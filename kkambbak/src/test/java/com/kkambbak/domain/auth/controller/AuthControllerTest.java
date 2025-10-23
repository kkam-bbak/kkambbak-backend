package com.kkambbak.domain.auth.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.domain.auth.facade.AuthFacade;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private AuthFacade authFacade;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_OTP_CODE = "123456";
    private static final String TEST_VERIFICATION_CODE = "12345678-1234-1234-1234-123456789012";
    private static final String TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final String TEST_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @Test
    void getCurrentEmailTest() throws Exception {
        // given
        given(authFacade.getEmailByCode(anyString()))
                .willReturn(TEST_EMAIL);

        // when & then
        this.mockMvc.perform(get("/api/v1/auth/current-email")
                        .param("code", TEST_VERIFICATION_CODE))
                .andExpect(status().isOk())
                .andDo(document("auth-current-email",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Auth")
                                        .summary("현재 인증 이메일 조회")
                                        .description("임시 코드로 인증 대기 중인 이메일을 조회합니다. 만료된 임시코드는 사용할 수 없습니다. 소셜로그인으로 재로그인 하면 임시코드 재발급 가능.")
                                        .queryParameters(
                                                parameterWithName("code").description("OAuth2 로그인 후 받은 임시 검증 코드 (UUID)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.email").type(JsonFieldType.STRING).description("인증 대기 중인 사용자 이메일")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void verifyOtpTest() throws Exception {
        // given
        TokenDataDto mockResponse = TokenDataDto.builder()
                .grantType("Bearer")
                .accessToken(TEST_ACCESS_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .accessTokenExpiredAt(System.currentTimeMillis() + 3600000)
                .refreshTokenExpiredAt(System.currentTimeMillis() + 604800000)
                .build();

        given(authFacade.verifyOtp(anyString(), anyString())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "email", TEST_EMAIL,
                                "otpCode", TEST_OTP_CODE
                        ))))
                .andExpect(status().isOk())
                .andDo(document("auth-verify-otp",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Auth")
                                        .summary("OTP 검증 및 JWT 토큰 발급")
                                        .description("OTP 코드를 검증하고 액세스 토큰과 리프레시 토큰을 발급받습니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("otpCode").type(JsonFieldType.STRING).description("OTP 코드 (6자리)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (밀리초)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (밀리초)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void resendOtpTest() throws Exception {
        // given
        doNothing().when(authFacade).resendOtp(anyString());

        // when & then
        this.mockMvc.perform(post("/api/v1/auth/resend-otp")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "email", TEST_EMAIL
                        ))))
                .andExpect(status().isOk())
                .andDo(document("auth-resend-otp",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Auth")
                                        .summary("OTP 재발송")
                                        .description("새로운 OTP 코드를 이메일로 재발송합니다. " +
                                                "세션에 저장된 verificationEmail이 있으면 우선적으로 사용합니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일")
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