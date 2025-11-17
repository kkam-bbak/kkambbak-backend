package com.kkambbak.domain.learning.controller;

import com.kkambbak.domain.learning.dto.LearningStartDto;
import com.kkambbak.domain.learning.facade.LearningFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningStartController {

    private final LearningFacade learningFacade;

    @PostMapping("/sessions/{sessionId}/start")
    public ApiResponse<LearningStartDto.StartResponse> start(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sessionId,
            @RequestBody(required = false) LearningStartDto.StartRequest body
    ) {

        Long userId = userDetails.getUserId();

        if (body == null) {
            body = LearningStartDto.StartRequest.builder().build();
        }

        var response = learningFacade.startLearning(
                userId,
                sessionId,
                body
        );

        return ApiResponse.ok(response);
    }
}