package com.kkambbak.domain.user.service;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.repository.UserRepository;
import com.kkambbak.domain.user.exception.LogoutFailedException;
import com.kkambbak.global.jwt.JwtUtil;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public User createOrUpdateUser(String provider, String providerId, String email,
                                   String firstName, String lastName, String profileImage) {
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());

        return userRepository.findByProviderAndProviderId(authProvider, providerId)
                .map(existingUser -> {
                    return userRepository.save(
                            existingUser.updateFromOAuth2(email, firstName, lastName, profileImage)
                    );
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .profileImage(profileImage)
                            .provider(authProvider)
                            .providerId(providerId)
                            .isGuest(false)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    /**
     * 로그아웃: 토큰 블랙리스트 처리
     */
    @Transactional
    public void logout(UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserId();

        // 현재 요청에서 토큰 추출
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    jwtUtil.blacklistToken(token);
                    log.info("User logged out successfully - userId: {}", userId);
                } catch (Exception e) {
                    log.error("Failed to blacklist token for user: {}", userId, e);
                    throw new LogoutFailedException();
                }
            } else {
                log.warn("No authorization header found for logout - userId: {}", userId);
            }
        }
    }
}