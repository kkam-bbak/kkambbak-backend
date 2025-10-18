package com.kkambbak.domain.user.dto;

import com.kkambbak.global.jwt.dto.TokenDataDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LoginTokenDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestLoginRequest {
        private String email;
        private String key;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestLoginRequest {
        private String guestId;  // 선택사항 (있으면 기존 계정, 없으면 새로 생성)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private TokenDataDto tokenData;
        private Long userId;
        private String email;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestLoginResponse {
        private TokenDataDto tokenData;
        private Long userId;
        private String providerId; 
        private Boolean isGuest;
    }
}
