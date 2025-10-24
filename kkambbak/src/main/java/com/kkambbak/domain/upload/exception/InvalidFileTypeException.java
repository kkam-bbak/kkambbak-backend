package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidFileTypeException extends CustomException {
    public InvalidFileTypeException() {
        super(UploadErrorCode.INVALID_FILE_TYPE);
    }

    public InvalidFileTypeException(String message) {
        super(UploadErrorCode.INVALID_FILE_TYPE, message);
    }
}