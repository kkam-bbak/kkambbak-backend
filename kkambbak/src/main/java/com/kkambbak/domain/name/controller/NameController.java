package com.kkambbak.domain.name.controller;


import com.kkambbak.domain.name.dto.NameRequestDto;
import com.kkambbak.domain.name.dto.NameResponseDto;
import com.kkambbak.domain.name.facade.NameFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/name")
@RequiredArgsConstructor
public class NameController {

    private final NameFacade nameFacade;

    //한국어 이름 생성 API
    @PostMapping("/generate")
    public ApiResponse<NameResponseDto> createKoreanName(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody NameRequestDto nameRequestDto) {
        NameResponseDto nameCandidateResponse = nameFacade.generate(user.getUserId(), nameRequestDto);
        return ApiResponse.ok(nameCandidateResponse);
    }


}
