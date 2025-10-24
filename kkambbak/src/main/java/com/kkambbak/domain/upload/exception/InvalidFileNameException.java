package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InvalidFileNameException extends CustomException {
    public InvalidFileNameException() {
        super(UploadErrorCode.INVALID_FILE_NAME);
    }

    public InvalidFileNameException(String message) {
        super(UploadErrorCode.INVALID_FILE_NAME, message);
    }
}