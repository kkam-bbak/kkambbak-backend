package com.kkambbak.domain.upload.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadErrorCode implements ResponseCode {

    FILE_IS_EMPTY("UP001", "File is empty"),
    INVALID_FILE_TYPE("UP002", "Invalid file type. Only image files are allowed"),
    INVALID_FILE_NAME("UP003", "Invalid file name"),
    FILE_STORAGE_FAILED("UP004", "Failed to store file"),
    IMAGE_CONVERSION_FAILED("UP005", "Failed to convert image to WebP format"),
    IMAGE_RESIZE_FAILED("UP006", "Failed to resize image"),
    UNSUPPORTED_IMAGE_FORMAT("UP007", "Unsupported image format. Please convert HEIC to JPEG or PNG");

    private final String statusCode;
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }
}