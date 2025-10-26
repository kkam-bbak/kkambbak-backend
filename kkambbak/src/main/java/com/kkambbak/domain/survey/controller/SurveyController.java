package com.kkambbak.domain.survey.controller;

import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.facade.SurveyFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyFacade surveyFacade;

    // 설문 저장
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SurveyDto.SurveySaveResponse> save(
            @AuthenticationPrincipal UserDetailsImpl userDetails,   // 인증 성공 시에만 주입됨
            @Valid @RequestBody SurveyDto.SurveySaveRequest request
    ) {
        var body = surveyFacade.saveAndPrioritize(userDetails.getUserId(), request);
        return ApiResponse.ok(body);
    }

    // 설문 완료 여부 확인
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Map<String, Boolean>> check(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ApiResponse.ok(Map.of("completed", false));
        }
        boolean completed = surveyFacade.isCompleted(userDetails.getUserId());
        return ApiResponse.ok(Map.of("completed", completed));
    }
}