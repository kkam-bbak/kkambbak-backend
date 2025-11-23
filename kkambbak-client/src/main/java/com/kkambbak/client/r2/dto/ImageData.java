package com.kkambbak.client.r2.dto;

public record ImageData(
        byte[] bytes,
        String contentType
) {
}