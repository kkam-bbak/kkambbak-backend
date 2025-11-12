package com.kkambbak.domain.roleplay.controller;


import com.kkambbak.domain.roleplay.dto.RoleplayDialoguesResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import com.kkambbak.domain.roleplay.service.RoleplayService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


}
