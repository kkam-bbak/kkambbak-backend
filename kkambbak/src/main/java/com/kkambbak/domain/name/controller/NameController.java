package com.kkambbak.domain.name.controller;


import com.kkambbak.domain.name.dto.NameResponseDto;
import com.kkambbak.domain.name.dto.NameSelectRequestDto;
import com.kkambbak.domain.name.facade.NameFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
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
            @AuthenticationPrincipal UserDetailsImpl user) {
        NameResponseDto nameCandidateResponse = nameFacade.generate(user.getUserId());
        return ApiResponse.ok(nameCandidateResponse);
    }

    //한국어 이름 선택 API
    @PostMapping("/select")
    public ApiResponse<Void> selectKoreanName(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody @Valid NameSelectRequestDto nameSelectRequestDto){
        nameFacade.select(user.getUserId(), nameSelectRequestDto);
        return ApiResponse.ok();
    }


}
