package com.kkambbak.client.r2.exception;

import com.kkambbak.client.exception.ClientException;
import com.kkambbak.client.r2.enums.R2ErrorCode;

public class R2Exception extends ClientException {

    public R2Exception(String code, String message) {
        super(code, message);
    }

    public R2Exception(R2ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}