package com.kkambbak.global.util;

import com.kkambbak.domain.upload.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUpload {

    private final FileStorageService fileStorageService;

    // Base64 이미지 데이터를 URL로 변환
    public String convertBase64ToUrlIfNeeded(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try {
            if (data.startsWith("data:image") || data.length() > 500) {
                return fileStorageService.uploadBase64Image(data);
            }
            return data;
        } catch (Exception e) {
            log.error("Failed to convert Base64 image to URL", e);
            return data;
        }
    }
}