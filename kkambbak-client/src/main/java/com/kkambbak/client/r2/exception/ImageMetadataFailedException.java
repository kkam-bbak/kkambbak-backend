package com.kkambbak.client.r2.exception;

import com.kkambbak.client.r2.enums.R2ErrorCode;

public class ImageMetadataFailedException extends R2Exception {

    public ImageMetadataFailedException() {
        super(R2ErrorCode.IMAGE_METADATA_FAILED);
    }

    public ImageMetadataFailedException(String message) {
        super(R2ErrorCode.IMAGE_METADATA_FAILED.getCode(), message);
    }
}