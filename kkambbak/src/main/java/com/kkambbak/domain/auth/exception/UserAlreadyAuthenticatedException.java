package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class UserAlreadyAuthenticatedException extends CustomException {

    public UserAlreadyAuthenticatedException() {
        super(AuthErrorCode.USER_ALREADY_AUTHENTICATED);
    }
}