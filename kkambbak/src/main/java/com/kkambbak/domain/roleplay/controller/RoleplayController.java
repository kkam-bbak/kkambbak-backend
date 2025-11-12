package com.kkambbak.domain.roleplay.controller;


import com.kkambbak.domain.roleplay.dto.RoleplayDialoguesResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplayEvaluateResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import com.kkambbak.domain.roleplay.service.RoleplayService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/roleplay")
@RequiredArgsConstructor
public class RoleplayController {
    private final RoleplayFacade roleplayFacade;
    private final RoleplayService roleplayService;

    //롤플레이 시나리오 조회 API
    @GetMapping("/all")
    public ApiResponse<List<RoleplayResponseDto>> getAllRoleplayScenarios(@AuthenticationPrincipal UserDetailsImpl user) {
        List<RoleplayResponseDto> allRoleplayScenarios = roleplayService.getAllRoleplayScenarios();
        return ApiResponse.ok(allRoleplayScenarios);
    }

    //롤플레이 시작 API
    @PostMapping("/start")
    public ApiResponse<RoleplayDialoguesResponseDto> startRoleplay(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long scenarioId) {
        RoleplayDialoguesResponseDto response = roleplayFacade.start(user.getUserId(), scenarioId);
        return ApiResponse.ok(response);
    }

    //다음 문장 API
    @PostMapping("/next")
    public ApiResponse<RoleplayDialoguesResponseDto> next(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long sessionId) {
        RoleplayDialoguesResponseDto response = roleplayFacade.next(user.getUserId(), sessionId);
        return ApiResponse.ok(response);

    }

    //사용자 발음 평가 API
    @PostMapping("/evaluate")
    public ApiResponse<RoleplayEvaluateResponseDto> evaluatePronunciation(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long sessionId,
            @RequestParam Long dialogueId,
            @RequestParam MultipartFile audioFile) throws IOException, ExecutionException, InterruptedException, UnsupportedAudioFileException {
        RoleplayEvaluateResponseDto evaluateResult = roleplayFacade.evaluate(user.getUserId(), sessionId, dialogueId, audioFile);
        return ApiResponse.ok(evaluateResult);
    }


}
