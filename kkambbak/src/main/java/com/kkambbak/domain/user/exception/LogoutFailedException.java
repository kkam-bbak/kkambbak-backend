package com.kkambbak.domain.user.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class LogoutFailedException extends CustomException {

    public LogoutFailedException() {
        super(UserErrorCode.LOGOUT_FAILED);
    }
}