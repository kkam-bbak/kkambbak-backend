package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidRefreshTokenException extends CustomException {

    public InvalidRefreshTokenException() {
        super(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}