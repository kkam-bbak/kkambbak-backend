package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class VerificationCodeNotFoundException extends CustomException {

    public VerificationCodeNotFoundException() {
        super(AuthErrorCode.VERIFICATION_CODE_NOT_FOUND);
    }
}