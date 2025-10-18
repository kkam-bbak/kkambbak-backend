package com.kkambbak.domain.user.enums;

import com.kkambbak.core.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ResponseCode {

    USER_NOT_FOUND("U001", "User not found"),
    INVALID_AUTH_KEY("U002", "Invalid authentication key"),
    LOGOUT_FAILED("U003", "Logout failed"),
    GUEST_NOT_FOUND("U004", "Guest account not found"),
    INVALID_GUEST_ID("U005", "Invalid guest ID format");

    private final String statusCode;
    private final String message;
}