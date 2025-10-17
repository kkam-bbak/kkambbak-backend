package com.kkambbak.global.jwt.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.global.jwt.enums.JwtErrorCode;

public class InvalidJwtTokenException extends CustomException {
    public InvalidJwtTokenException() {
        super(JwtErrorCode.INVALID_JWT_TOKEN);
    }
}