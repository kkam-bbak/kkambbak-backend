package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OtpCodeInvalidException extends CustomException {

    public OtpCodeInvalidException() {
        super(AuthErrorCode.OTP_CODE_INVALID);
    }
}