package com.kkambbak.client.r2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum R2ErrorCode {
    IMAGE_NOT_FOUND("R2001", "The requested image could not be found."),
    IMAGE_DOWNLOAD_FAILED("R2002", "Failed to download image from R2."),
    IMAGE_METADATA_FAILED("R2003", "Failed to retrieve image metadata."),
    R2_SERVICE_ERROR("R2004", "R2 service error occurred.");

    private final String code;
    private final String message;
}