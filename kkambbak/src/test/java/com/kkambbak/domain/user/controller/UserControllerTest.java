package com.kkambbak.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.domain.user.dto.UpdateProfileDto;
import com.kkambbak.domain.user.dto.GetProfileDto;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        given(userService.testLoginByEmail(anyString(), anyString())).willReturn(mockResponse);

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
        given(userService.refreshToken(anyString())).willReturn(mockTokenData);

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
    void guestLoginTest() throws Exception {
        // given
        LoginTokenDto.GuestLoginResponse mockGuestResponse = LoginTokenDto.GuestLoginResponse.builder()
                .providerId("guest_550e8400-e29b-41d4-a716-446655440000")
                .isGuest(true)
                .tokenData(TokenDataDto.builder()
                        .grantType("Bearer")
                        .accessToken("mockGuestAccessToken")
                        .refreshToken("mockGuestRefreshToken")
                        .accessTokenExpiredAt(System.currentTimeMillis() + 3600000)
                        .refreshTokenExpiredAt(System.currentTimeMillis() + 86400000)
                        .build())
                .build();
        given(userService.guestLogin(any())).willReturn(mockGuestResponse);

        // when & then
        this.mockMvc.perform(post("/api/v1/users/guest-login")
                        .contentType("application/json")
                        .content(toJson(Map.of("guestId", "guest_550e8400-e29b-41d4-a716-446655440000"))))
                .andExpect(status().isOk())
                .andDo(document("user-guest-login",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("게스트 로그인")
                                        .description("게스트 사용자를 생성하거나 기존 게스트 계정으로 로그인하고 액세스 토큰과 리프레시 토큰을 발급받습니다. " +
                                                "guestId를 보내면 기존 게스트 계정으로 로그인하고, 보내지 않으면 새로운 게스트 계정을 생성합니다.")
                                        .requestFields(
                                                fieldWithPath("guestId").type(JsonFieldType.STRING).description("게스트 ID (선택사항, 있으면 기존 계정 조회, 없으면 새로 생성. 예: guest_550e8400-e29b-41d4-a716-446655440000)").optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.providerId").type(JsonFieldType.STRING).description("게스트 Provider ID (guest_로 시작)"),
                                                fieldWithPath("body.isGuest").type(JsonFieldType.BOOLEAN).description("게스트 여부 (true)"),
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

    @Test
    void registerTest() throws Exception {
        // given
        given(userService.validateProfileRequest(any(UpdateProfileDto.class))).willReturn(Gender.MALE);
        doNothing().when(userService).register(anyLong(), any(UpdateProfileDto.class), any(Gender.class));

        // when & then
        this.mockMvc.perform(patch("/api/v1/users/register")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "name", "Kim Jun Hyeong",
                                "gender", "MALE",
                                "countryOfOrigin", "South Korea",
                                "profileImage", "https://example.com/profile.jpg"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-register",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("사용자 정보 회원가입")
                                        .description("소셜 로그인 또는 게스트 로그인 후 회원가입 진행합니다. 이름, 성별, 국가는 필수이며 프로필 사진 URL은 선택사항입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .requestFields(
                                                fieldWithPath("name").type(JsonFieldType.STRING).description("영문 이름 (필수)"),
                                                fieldWithPath("gender").type(JsonFieldType.STRING).description("성별 (필수, MALE, FEMALE)"),
                                                fieldWithPath("countryOfOrigin").type(JsonFieldType.STRING).description("국가명 (필수)"),
                                                fieldWithPath("profileImage").type(JsonFieldType.STRING).description("프로필 사진 URL (선택)").optional()
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

    @Test
    void registerKoreanTest() throws Exception {
        // given
        doNothing().when(userService).validateKoreanNameRequest(any());
        doNothing().when(userService).registerKorean(anyLong(), any());

        // when & then
        this.mockMvc.perform(post("/api/v1/users/register-korean")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "preferredNameMeaning", "My name means to shine brightly like light and bring warmth to others.",
                                "personalityOrImage", "I'm full of bright energy with a playful, charming vibe."
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-register-korean",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("한국어 이름 생성을 위한 회원가입")
                                        .description("원하는 이름의 의미와 본인이 생각하는 자기의 성격/이미지 설명을 저장합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .requestFields(
                                                fieldWithPath("preferredNameMeaning").type(JsonFieldType.STRING).description("선호하는 이름 의미"),
                                                fieldWithPath("personalityOrImage").type(JsonFieldType.STRING).description("성격/이미지 설명")
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

    @Test
    void getProfileTest() throws Exception {
        // given
        GetProfileDto mockProfile = GetProfileDto.builder()
                .name("Kim Jun Hyeong")
                .koreanName("김준형")
                .nameMeaning("준하는 의미는 밝고 긍정적인 에너지를 의미합니다")
                .gender(Gender.MALE)
                .countryOfOrigin("South Korea")
                .personalityOrImage("I'm full of bright energy with a playful, charming vibe.")
                .profileImage("https://example.com/profile.jpg")
                .remainingNameAttempts(1)
                .build();
        given(userService.getProfile(anyLong())).willReturn(mockProfile);

        // when & then
        this.mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", "Bearer access_token_example"))
                .andExpect(status().isOk())
                .andDo(document("user-get-profile",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Users")
                                        .summary("프로필 조회")
                                        .description("사용자의 프로필 정보를 조회합니다")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.name").type(JsonFieldType.STRING).description("영문 이름"),
                                                fieldWithPath("body.koreanName").type(JsonFieldType.STRING).description("한국어 이름"),
                                                fieldWithPath("body.nameMeaning").type(JsonFieldType.STRING).description("한국어 이름 뜻"),
                                                fieldWithPath("body.gender").type(JsonFieldType.STRING).description("성별 (MALE, FEMALE)"),
                                                fieldWithPath("body.countryOfOrigin").type(JsonFieldType.STRING).description("국가명"),
                                                fieldWithPath("body.personalityOrImage").type(JsonFieldType.STRING).description("성격/이미지 설명"),
                                                fieldWithPath("body.profileImage").type(JsonFieldType.STRING).description("프로필 사진 URL"),
                                                fieldWithPath("body.remainingNameAttempts").type(JsonFieldType.NUMBER).description("한국어 이름 생성 남은 횟수")
                                        )
                                        .build()
                        )
                ));
    }
}