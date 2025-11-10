package com.kkambbak.domain.roleplay.controller;


import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import com.kkambbak.domain.roleplay.service.RoleplayService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/roleplay")
@RequiredArgsConstructor
public class RoleplayController {
    private final RoleplayFacade roleplayFacade;
    private final RoleplayService roleplayService;

    @GetMapping("/all")
    public ApiResponse<List<RoleplayResponseDto>> getAllRoleplayScenarios(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<RoleplayResponseDto> allRoleplayScenarios = roleplayService.getAllRoleplayScenarios();
        return ApiResponse.ok(allRoleplayScenarios);
    }



}
