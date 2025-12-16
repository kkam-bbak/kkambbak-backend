package com.kkambbak.global.jwt.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.global.jwt.enums.JwtErrorCode;

public class UnsupportedJwtTokenException extends CustomException {
    public UnsupportedJwtTokenException() {
        super(JwtErrorCode.UNSUPPORTED_JWT_TOKEN);
    }
}