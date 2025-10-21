package com.kkambbak.domain.roleplay.controller;


import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.facade.RoleplayFacade;
import com.kkambbak.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/all")
    public ApiResponse<List<RoleplayResponseDto>> getAllRoleplayScenarios() {
        List<RoleplayResponseDto> allRoleplayScenarios = roleplayFacade.getAllRoleplayScenarios();
        return ApiResponse.ok(allRoleplayScenarios);
    }


}
