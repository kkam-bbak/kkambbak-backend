package com.kkambbak.global.jwt.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.global.jwt.enums.JwtErrorCode;

public class ExpiredJwtTokenException extends CustomException {
    public ExpiredJwtTokenException() {
        super(JwtErrorCode.EXPIRED_JWT_TOKEN);
    }
}