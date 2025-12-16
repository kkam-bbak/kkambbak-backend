package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OtpCodeExpiredException extends CustomException {

    public OtpCodeExpiredException() {
        super(AuthErrorCode.OTP_CODE_EXPIRED);
    }
}
