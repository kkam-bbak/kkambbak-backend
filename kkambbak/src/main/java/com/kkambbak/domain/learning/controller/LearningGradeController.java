package com.kkambbak.domain.learning.controller;

import com.kkambbak.domain.learning.dto.LearningGradeDto;
import com.kkambbak.domain.learning.service.LearningGradeService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningGradeController {

    private final LearningGradeService learningGradeService;
    @PostMapping("/{sessionId}/grade")
    public ApiResponse<LearningGradeDto.GradeResponse> grade(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sessionId,
            @RequestBody LearningGradeDto.GradeRequest request
    ) {
        Long userId = userDetails.getUserId();
        var response = learningGradeService.grade(userId, sessionId, request);
        return ApiResponse.ok(response);
    }
}