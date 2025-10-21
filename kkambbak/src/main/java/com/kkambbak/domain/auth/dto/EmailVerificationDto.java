package com.kkambbak.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class EmailVerificationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyOtpRequest {
        private String email;
        private String otpCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResendOtpRequest {
        private String email;
    }
}