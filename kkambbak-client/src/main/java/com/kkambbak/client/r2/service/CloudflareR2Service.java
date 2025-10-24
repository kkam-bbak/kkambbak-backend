package com.kkambbak.client.r2.service;

import com.kkambbak.client.r2.config.CloudflareR2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudflareR2Service implements ImageUploader {

    private final CloudflareR2Config r2Config;
    private final S3Client r2S3Client;

    @Override
    public String uploadImage(byte[] imageBytes, String filename) throws Exception {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(filename)
                    .contentType("image/webp")
                    .build();

            PutObjectResponse response = r2S3Client.putObject(
                    putRequest,
                    RequestBody.fromBytes(imageBytes)
            );

            // R2 공개 URL 생성
            String imageUrl = r2Config.getPublicDomain() + "/" + filename;

            log.info("Image uploaded to Cloudflare R2: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("Failed to upload image to Cloudflare R2: {}", filename, e);
            throw new Exception("Failed to upload image to Cloudflare R2", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) throws Exception {
        try {
            return uploadImage(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Failed to read MultipartFile", e);
            throw new Exception("Failed to read file", e);
        }
    }
}