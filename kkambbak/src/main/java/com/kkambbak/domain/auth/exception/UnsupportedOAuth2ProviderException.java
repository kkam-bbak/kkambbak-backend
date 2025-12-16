package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class UnsupportedOAuth2ProviderException extends CustomException {

    public UnsupportedOAuth2ProviderException(String provider) {
        super(AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
    }
}