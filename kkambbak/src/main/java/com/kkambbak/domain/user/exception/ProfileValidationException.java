package com.kkambbak.domain.user.exception;

import com.kkambbak.domain.user.enums.UserErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ProfileValidationException extends CustomException {
    public ProfileValidationException(String message) {
        super(UserErrorCode.PROFILE_VALIDATION_FAILED, message);
    }

    public ProfileValidationException() {
        super(UserErrorCode.PROFILE_VALIDATION_FAILED);
    }
}