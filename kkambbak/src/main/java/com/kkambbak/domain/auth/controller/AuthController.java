package com.kkambbak.domain.auth.controller;

import com.kkambbak.domain.auth.dto.EmailVerificationDto;
import com.kkambbak.domain.auth.facade.AuthFacade;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import com.kkambbak.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @GetMapping("/current-email")
    public ApiResponse<Map<String, String>> getCurrentEmail(@RequestParam String code) {
        String email = authFacade.getEmailByCode(code);
        return ApiResponse.ok(Map.of("email", email));
    }

    @PostMapping("/verify-email")
    public ApiResponse<TokenDataDto> verifyOtp(
            @RequestBody EmailVerificationDto.VerifyOtpRequest request) {
        var response = authFacade.verifyOtp(request.getEmail(), request.getOtpCode());
        return ApiResponse.ok(response);
    }

    @PostMapping("/resend-otp")
    public ApiResponse<?> resendOtp(@RequestBody EmailVerificationDto.ResendOtpRequest request) {
        authFacade.resendOtp(request.getEmail());
        return ApiResponse.ok();
    }
}