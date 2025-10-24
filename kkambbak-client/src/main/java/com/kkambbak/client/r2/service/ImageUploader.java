package com.kkambbak.client.r2.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {

    /**
     * byte array를 Cloudflare R2에 업로드
     *
     * @param imageBytes 이미지 바이트 배열
     * @param filename   파일명 (UUID 권장)
     * @return 업로드된 이미지 URL
     * @throws Exception R2 업로드 실패 시
     */
    String uploadImage(byte[] imageBytes, String filename) throws Exception;

    /**
     * MultipartFile을 Cloudflare R2에 업로드
     *
     * @param file MultipartFile
     * @return 업로드된 이미지 URL
     * @throws Exception R2 업로드 실패 시
     */
    String uploadImage(MultipartFile file) throws Exception;
}