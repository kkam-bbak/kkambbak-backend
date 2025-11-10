package com.kkambbak.client.r2.service;

import com.kkambbak.client.r2.config.CloudflareR2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudflareR2AudioService {
    private final CloudflareR2Config r2Config;
    private final S3Client r2S3Client;

    public String uploadAudio(byte[] bytes, String filename) throws Exception {
        return upload(bytes, "tts/" + filename, "audio/wav");
    }

    private String upload(byte[] bytes, String path, String contentType) throws Exception {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(path)
                .contentType(contentType)
                .build();

        r2S3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        return r2Config.getPublicDomain() + "/" + path;
    }

}
