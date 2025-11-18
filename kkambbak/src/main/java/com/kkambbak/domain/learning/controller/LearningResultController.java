package com.kkambbak.domain.learning.controller;

import com.kkambbak.domain.learning.dto.LearningResultDto;
import com.kkambbak.domain.learning.service.LearningResultQueryService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningResultController {

    private final LearningResultQueryService learningResultQueryService;

    // 결과 요약 조회
    @GetMapping("/{sessionId}/results/summary")
    public ApiResponse<LearningResultDto.SummaryResponse> getLatestSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sessionId
    ) {
        Long userId = userDetails.getUserId();
        LearningResultDto.SummaryResponse summary =
                learningResultQueryService.getLatestSummary(userId, sessionId);
        return ApiResponse.ok(summary);
    }

    // 결과 + 정오답 리스트 조회
    @GetMapping("/{sessionId}/results/review")
    public ApiResponse<LearningResultDto.ReviewResponse> getLatestReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sessionId
    ) {
        Long userId = userDetails.getUserId();
        LearningResultDto.ReviewResponse review =
                learningResultQueryService.getLatestReview(userId, sessionId);
        return ApiResponse.ok(review);
    }
}