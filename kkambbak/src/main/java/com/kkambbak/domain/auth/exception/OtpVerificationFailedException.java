package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OtpVerificationFailedException extends CustomException {

    public OtpVerificationFailedException() {
        super(AuthErrorCode.OTP_VERIFICATION_FAILED);
    }
}