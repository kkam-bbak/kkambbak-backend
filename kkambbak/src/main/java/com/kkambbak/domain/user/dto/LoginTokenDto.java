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
    public static class Response {
        private TokenDataDto tokenData;
        private Long userId;
        private String email;
    }
}
