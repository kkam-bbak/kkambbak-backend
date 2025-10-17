package com.kkambbak.domain.auth.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ResponseCode {

    INVALID_REFRESH_TOKEN("AT001", "Invalid refresh token"),
    TOKEN_REFRESH_FAILED("AT002", "Token refresh failed"),
    USER_NOT_FOUND("AT003", "User not found");

    private final String statusCode;
    private final String message;
}