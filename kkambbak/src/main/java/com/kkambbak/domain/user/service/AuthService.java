package com.kkambbak.domain.user.service;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.UserRepository;
import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.domain.user.exception.InvalidAuthKeyException;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import com.kkambbak.global.jwt.JwtUtil;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.auth.key}")
    private String authKey;

    /**
     * Refresh Token으로 토큰 발급
     */
    @Transactional(readOnly = true)
    public TokenDataDto refreshToken(String refreshToken) {
        return jwtUtil.refreshToken(refreshToken);
    }

    /**
     * 테스트용: 이메일과 암호키로 토큰 발급
     */
    @Transactional(readOnly = true)
    public LoginTokenDto.Response testLoginByEmail(String email, String key) {
        validateAuthKey(key);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        TokenDataDto tokenData = jwtUtil.createTokenData(user.getId());

        return LoginTokenDto.Response.builder()
                .tokenData(tokenData)
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    private void validateAuthKey(String key) {
        if (!authKey.equals(key)) {
            log.warn("Invalid authentication key attempt");
            throw new InvalidAuthKeyException();
        }
    }
}