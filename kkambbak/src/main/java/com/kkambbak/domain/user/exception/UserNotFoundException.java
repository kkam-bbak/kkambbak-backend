package com.kkambbak.domain.user.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}