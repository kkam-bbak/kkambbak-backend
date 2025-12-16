package com.kkambbak.client.r2.exception;

import com.kkambbak.client.r2.enums.R2ErrorCode;

public class ImageDownloadFailedException extends R2Exception {

    public ImageDownloadFailedException() {
        super(R2ErrorCode.IMAGE_DOWNLOAD_FAILED);
    }

    public ImageDownloadFailedException(String message) {
        super(R2ErrorCode.IMAGE_DOWNLOAD_FAILED.getCode(), message);
    }
}