package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ImageConversionFailedException extends CustomException {
    public ImageConversionFailedException() {
        super(UploadErrorCode.IMAGE_CONVERSION_FAILED);
    }

    public ImageConversionFailedException(String message) {
        super(UploadErrorCode.IMAGE_CONVERSION_FAILED, message);
    }
}