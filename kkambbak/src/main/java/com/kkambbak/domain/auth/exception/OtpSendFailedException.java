package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OtpSendFailedException extends CustomException {

    public OtpSendFailedException() {
        super(AuthErrorCode.OTP_SEND_FAILED);
    }
}