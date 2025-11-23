package com.kkambbak.client.r2.service;

import com.kkambbak.client.r2.config.CloudflareR2Config;
import com.kkambbak.client.r2.exception.ImageDownloadFailedException;
import com.kkambbak.client.r2.exception.ImageMetadataFailedException;
import com.kkambbak.client.r2.exception.ImageNotFoundException;
import com.kkambbak.client.r2.exception.R2ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2ImageService {

    private final CloudflareR2Config r2Config;
    private final S3Client r2S3Client;

    public byte[] downloadImage(String imageId) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(imageId)
                    .build();

            return r2S3Client.getObject(getRequest).readAllBytes();
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

    public String getContentType(String imageId) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(imageId)
                    .build();

            HeadObjectResponse headResponse = r2S3Client.headObject(headRequest);
            String contentType = headResponse.contentType();

            return contentType != null ? contentType : "image/webp";
        } catch (NoSuchKeyException e) {
            throw new ImageNotFoundException();
        } catch (Exception e) {
            log.error("[R2] Content-Type 조회 실패: {}", imageId, e);
            throw new ImageMetadataFailedException();
        }
    }
}