package com.kkambbak.domain.upload.exception;

import com.kkambbak.domain.upload.enums.UploadErrorCode;
import com.kkambbak.global.exception.CustomException;

public class FileStorageFailedException extends CustomException {
    public FileStorageFailedException() {
        super(UploadErrorCode.FILE_STORAGE_FAILED);
    }

    public FileStorageFailedException(String message) {
        super(UploadErrorCode.FILE_STORAGE_FAILED, message);
    }
}