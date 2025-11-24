package com.kkambbak.domain.learning.controller;

import com.kkambbak.core.entity.learning.enums.GradeAction;
import com.kkambbak.domain.learning.dto.LearningGradeDto;
import com.kkambbak.domain.learning.facade.LearningFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningGradeController {

    private final LearningFacade learningFacade;

    @PostMapping(
            value = "/{sessionId}/grade",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<LearningGradeDto.GradeResponse> grade(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sessionId,
            @RequestParam("action") GradeAction action,
            @RequestParam("itemId") Long itemId,
            @RequestParam("resultId") Long resultId,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile
    ) {

        Long userId = userDetails.getUserId();

        // DTO 조립
        LearningGradeDto.GradeRequest request = LearningGradeDto.GradeRequest.builder()
                .action(action)
                .itemId(itemId)
                .resultId(resultId)
                .build();

        var response = learningFacade.gradeLearning(
                userId,
                sessionId,
                request,
                audioFile
        );
        return ApiResponse.ok(response);
    }
}