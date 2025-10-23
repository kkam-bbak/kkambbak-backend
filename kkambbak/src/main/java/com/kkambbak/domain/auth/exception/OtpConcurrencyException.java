package com.kkambbak.domain.auth.exception;

import com.kkambbak.domain.auth.enums.AuthErrorCode;
import com.kkambbak.global.exception.CustomException;

public class OtpConcurrencyException extends CustomException {

    public OtpConcurrencyException(String message) {
        super(AuthErrorCode.OTP_CONCURRENCY_ERROR);
    }
}