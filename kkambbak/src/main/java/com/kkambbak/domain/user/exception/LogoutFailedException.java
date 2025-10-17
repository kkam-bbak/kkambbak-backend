package com.kkambbak.domain.user.exception;

import com.kkambbak.core.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class LogoutFailedException extends CustomException {

    public LogoutFailedException() {
        super(UserErrorCode.LOGOUT_FAILED);
    }
}