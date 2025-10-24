package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class FileIsEmptyException extends CustomException {
    public FileIsEmptyException() {
        super(UploadErrorCode.FILE_IS_EMPTY);
    }

    public FileIsEmptyException(String message) {
        super(UploadErrorCode.FILE_IS_EMPTY, message);
    }
}