package com.kkambbak.domain.upload.service;

import com.kkambbak.client.r2.dto.ImageData;
import com.kkambbak.client.r2.service.ImageUploader;
import com.kkambbak.client.r2.service.R2ImageService;
import com.kkambbak.domain.upload.exception.*;
import com.kkambbak.global.util.ImageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ImageUploader imageUploader;
    private final R2ImageService r2ImageService;

    // 이미지 파일을 업로드하고 URL을 반환
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileIsEmptyException();
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException();
        }

        return storeFile(file);
    }
    // 파일을 WebP 포맷으로 변환후 업로드
    private String storeFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

        if (originalFilename.contains("..")) {
            throw new InvalidFileNameException();
        }

        // HEIC/HEIF 파일 검사 및 거부
        String filenameLower = originalFilename.toLowerCase();
        if (filenameLower.endsWith(".heic") || filenameLower.endsWith(".heif")) {
            log.warn("Unsupported image format rejected: {}", originalFilename);
            throw new UnsupportedImageFormatException("HEIC/HEIF 형식은 지원하지 않습니다. JPEG 또는 PNG로 변환 후 업로드해주세요.");
        }

        try (InputStream webpInputStream = ImageConverter.convertToWebP(file)) {
            byte[] fileBytes = webpInputStream.readAllBytes();

            String storedFilename = UUID.randomUUID().toString();
            return imageUploader.uploadImage(fileBytes, storedFilename);

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFilename, ex);
            throw new FileStorageFailedException(originalFilename);
        } catch (Exception ex) {
            log.error("Failed to upload to ImgBB: {}", originalFilename, ex);
            throw new FileStorageFailedException(originalFilename);
        }
    }

    // Base64 인코딩된 이미지를 URL을 반환
    public String uploadBase64Image(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }

        try {
            String base64Image = base64Data;
            if (base64Data.contains(",")) {
                base64Image = base64Data.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            try (InputStream imageInputStream = new ByteArrayInputStream(imageBytes);
                 InputStream webpInputStream = ImageConverter.convertToWebP(imageInputStream)) {
                byte[] webpBytes = webpInputStream.readAllBytes();

                String storedFilename = UUID.randomUUID().toString();
                return imageUploader.uploadImage(webpBytes, storedFilename);
            }

        } catch (Exception ex) {
            log.error("Failed to store base64 image", ex);
            throw new FileStorageFailedException("base64-image");
        }
    }

    public ImageData getImage(String imageId) {
        return r2ImageService.getImage(imageId);
    }
}