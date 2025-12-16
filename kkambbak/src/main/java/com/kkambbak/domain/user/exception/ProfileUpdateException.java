package com.kkambbak.domain.user.exception;

import com.kkambbak.domain.user.enums.UserErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ProfileUpdateException extends CustomException {
    public ProfileUpdateException(String message) {
        super(UserErrorCode.PROFILE_UPDATE_FAILED, message);
    }

    public ProfileUpdateException() {
        super(UserErrorCode.PROFILE_UPDATE_FAILED);
    }
}