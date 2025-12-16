package com.kkambbak.client.r2.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {

    String uploadImage(byte[] imageBytes, String filename) throws Exception;

    String uploadImage(MultipartFile file) throws Exception;
}