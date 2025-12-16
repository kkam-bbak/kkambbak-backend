package com.kkambbak.domain.auth.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ResponseCode {

    INVALID_REFRESH_TOKEN("AT001", "Invalid refresh token"),
    TOKEN_REFRESH_FAILED("AT002", "Token refresh failed"),
    USER_NOT_FOUND("AT003", "User not found"),
    OTP_VERIFICATION_FAILED("AT004", "OTP verification failed"),
    VERIFICATION_CODE_NOT_FOUND("AT005", "Verification code not found"),
    VERIFICATION_CODE_ALREADY_USED("AT006", "Verification code already used"),
    OTP_CODE_EXPIRED("AT007", "OTP code expired"),
    OTP_CODE_INVALID("AT008", "OTP code is invalid"),
    OTP_CONCURRENCY_ERROR("AT009", "OTP concurrency error occurred"),
    OTP_SEND_FAILED("AT010", "OTP send failed"),
    USER_ALREADY_AUTHENTICATED("AT011", "User already authenticated"),
    OAUTH2_AUTHENTICATION_FAILED("AT012", "OAuth2 authentication failed"),
    UNSUPPORTED_OAUTH2_PROVIDER("AT013", "Unsupported OAuth2 provider");

    private final String statusCode;
    private final String message;
}