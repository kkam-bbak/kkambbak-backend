package com.kkambbak.client.r2.service;

import com.kkambbak.client.r2.config.CloudflareR2Config;
import com.kkambbak.client.r2.dto.ImageData;
import com.kkambbak.client.r2.exception.ImageDownloadFailedException;
import com.kkambbak.client.r2.exception.ImageNotFoundException;
import com.kkambbak.client.r2.exception.R2ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2ImageService {

    private final CloudflareR2Config r2Config;
    private final S3Client r2S3Client;

    public ImageData getImage(String imageId) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(imageId)
                    .build();

            try (var responseInputStream = r2S3Client.getObject(getRequest)) {
                byte[] imageBytes = responseInputStream.readAllBytes();
                GetObjectResponse response = responseInputStream.response();
                String contentType = response.contentType();
                contentType = contentType != null ? contentType : "image/webp";
                return new ImageData(imageBytes, contentType);
            }
        } catch (NoSuchKeyException e) {
            throw new ImageNotFoundException();
        } catch (IOException e) {
            log.error("[R2] 이미지 다운로드 중 IO 오류: {}", imageId, e);
            throw new ImageDownloadFailedException();
        } catch (Exception e) {
            log.error("[R2] 이미지 다운로드 실패: {}", imageId, e);
            throw new R2ServiceException();
        }
    }
}