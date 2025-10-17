package com.kkambbak.domain.user.exception;

import com.kkambbak.core.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class InvalidAuthKeyException extends CustomException {

    public InvalidAuthKeyException() {
        super(UserErrorCode.INVALID_AUTH_KEY);
    }
}