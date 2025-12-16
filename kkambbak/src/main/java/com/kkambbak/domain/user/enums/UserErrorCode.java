package com.kkambbak.domain.user.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ResponseCode {

    USER_NOT_FOUND("US001", "User not found"),
    INVALID_AUTH_KEY("US002", "Invalid authentication key"),
    LOGOUT_FAILED("US003", "Logout failed"),
    GUEST_NOT_FOUND("US004", "Guest account not found"),
    INVALID_GUEST_ID("US005", "Invalid guest ID format"),
    PROFILE_UPDATE_FAILED("US006", "Profile update failed"),
    PROFILE_VALIDATION_FAILED("US007", "Profile validation failed");

    private final String statusCode;
    private final String message;
}