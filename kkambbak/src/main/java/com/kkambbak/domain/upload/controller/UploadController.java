package com.kkambbak.domain.upload.controller;

import com.kkambbak.domain.upload.service.FileStorageService;
import com.kkambbak.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        String fileUrl = fileStorageService.uploadImage(file);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);

        return ApiResponse.ok(response);
    }
}