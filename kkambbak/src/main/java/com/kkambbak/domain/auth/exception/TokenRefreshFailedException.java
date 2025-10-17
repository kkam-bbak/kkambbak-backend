package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class TokenRefreshFailedException extends CustomException {

    public TokenRefreshFailedException() {
        super(AuthErrorCode.TOKEN_REFRESH_FAILED);
    }
}