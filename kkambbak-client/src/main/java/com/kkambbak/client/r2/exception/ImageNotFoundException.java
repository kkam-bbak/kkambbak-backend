package com.kkambbak.client.r2.exception;

import com.kkambbak.client.r2.enums.R2ErrorCode;

public class ImageNotFoundException extends R2Exception {

    public ImageNotFoundException() {
        super(R2ErrorCode.IMAGE_NOT_FOUND);
    }

    public ImageNotFoundException(String message) {
        super(R2ErrorCode.IMAGE_NOT_FOUND.getCode(), message);
    }
}