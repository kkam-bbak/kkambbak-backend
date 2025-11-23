package com.kkambbak.client.r2.exception;

import com.kkambbak.client.r2.enums.R2ErrorCode;

public class R2ServiceException extends R2Exception {

    public R2ServiceException() {
        super(R2ErrorCode.R2_SERVICE_ERROR);
    }

    public R2ServiceException(String message) {
        super(R2ErrorCode.R2_SERVICE_ERROR.getCode(), message);
    }
}