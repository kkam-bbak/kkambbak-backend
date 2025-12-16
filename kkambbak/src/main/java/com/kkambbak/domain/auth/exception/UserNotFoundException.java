package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super(AuthErrorCode.USER_NOT_FOUND);
    }
}