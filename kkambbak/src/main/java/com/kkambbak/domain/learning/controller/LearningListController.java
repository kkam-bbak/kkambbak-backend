package com.kkambbak.domain.learning.controller;

import com.kkambbak.core.entity.survey.enums.CategoryType;
import com.kkambbak.domain.learning.dto.LearningSessionListResponse;
import com.kkambbak.domain.learning.service.LearningListService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningListController {

    private final LearningListService learningListService;

    @GetMapping("/sessions")
    public ApiResponse<LearningSessionListResponse> getLearningList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "category") CategoryType category,
            @RequestParam(name = "surveyKey", required = false) String surveyKey,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        Long userId = userDetails.getUserId();

        LearningSessionListResponse response = learningListService.getLearningList(
                userId, category, surveyKey, cursor, limit
        );
        return ApiResponse.ok(response);
    }
}