package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OAuth2AuthenticationException extends CustomException {

    public OAuth2AuthenticationException(String message) {
        super(AuthErrorCode.OAUTH2_AUTHENTICATION_FAILED);
    }

    public OAuth2AuthenticationException(String message, Throwable cause) {
        super(AuthErrorCode.OAUTH2_AUTHENTICATION_FAILED);
    }
}