package com.kkambbak.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisOtpDto {
    private Long userId;
    private String email;
    private String otpCode;
    private String verificationCode;
    private LocalDateTime expiresAt;
}