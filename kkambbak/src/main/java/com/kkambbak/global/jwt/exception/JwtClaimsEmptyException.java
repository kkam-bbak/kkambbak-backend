package com.kkambbak.global.jwt.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.global.jwt.enums.JwtErrorCode;

public class JwtClaimsEmptyException extends CustomException {
    public JwtClaimsEmptyException() {
        super(JwtErrorCode.JWT_CLAIMS_EMPTY);
    }
}