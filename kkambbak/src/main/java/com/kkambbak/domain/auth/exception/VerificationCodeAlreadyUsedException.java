package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class VerificationCodeAlreadyUsedException extends CustomException {

    public VerificationCodeAlreadyUsedException() {
        super(AuthErrorCode.VERIFICATION_CODE_ALREADY_USED);
    }
}