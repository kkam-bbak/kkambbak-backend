package com.kkambbak.domain.user.controller;

import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.domain.user.service.UserService;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 테스트용: 이메일과 키로 로그인하여 토큰 발급
     */
    @PostMapping("/test-login")
    public ApiResponse<LoginTokenDto.Response> testLogin(@Valid @RequestBody LoginTokenDto.TestLoginRequest request) {
        return ApiResponse.ok(userService.testLoginByEmail(request.getEmail(), request.getKey()));
    }

    @PostMapping("/guest-login")
    public ApiResponse<LoginTokenDto.GuestLoginResponse> guestLogin(
            @RequestBody(required = false) LoginTokenDto.GuestLoginRequest request) {
        String guestId = request != null ? request.getGuestId() : null;
        return ApiResponse.ok(userService.guestLogin(guestId));
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenDataDto> refreshToken(@RequestHeader("RefreshToken") String refreshToken) {
        return ApiResponse.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.logout(userDetails);
        return ApiResponse.ok();
    }
}
