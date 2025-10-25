package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class UnsupportedImageFormatException extends CustomException {
    public UnsupportedImageFormatException() {
        super(UploadErrorCode.UNSUPPORTED_IMAGE_FORMAT);
    }

    public UnsupportedImageFormatException(String message) {
        super(UploadErrorCode.UNSUPPORTED_IMAGE_FORMAT, message);
    }
}