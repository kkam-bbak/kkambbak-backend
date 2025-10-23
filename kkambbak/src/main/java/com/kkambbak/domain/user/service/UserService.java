package com.kkambbak.domain.user.service;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.domain.user.exception.InvalidAuthKeyException;
import com.kkambbak.domain.user.exception.LogoutFailedException;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import com.kkambbak.domain.user.exception.GuestNotFoundException;
import com.kkambbak.domain.user.exception.InvalidGuestIdException;
import com.kkambbak.global.jwt.JwtUtil;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.auth.key}")
    private String authKey;

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

    // 게스트 사용자 생성
    @Transactional
    public User createGuestUser() {
        String providerId = "guest_" + UUID.randomUUID();
        String lastNameMasked = providerId.substring(Math.max(0, providerId.length() - 8));

        User guestUser = User.builder()
                .firstName("Guest")
                .lastName(lastNameMasked)
                .email(null)
                .provider(AuthProvider.GUEST)
                .providerId(providerId)
                .isGuest(true)
                .build();

        return userRepository.save(guestUser);
    }

    @Transactional
    public LoginTokenDto.GuestLoginResponse guestLogin(String providerId) {
        User guestUser;

        if (providerId != null && !providerId.isEmpty()) {
            if (!providerId.startsWith("guest_")) {
                throw new InvalidGuestIdException();
            }

            guestUser = userRepository.findByProviderAndProviderId(AuthProvider.GUEST, providerId)
                    .orElseThrow(() -> {
                        log.warn("Guest not found with providerId: {}", providerId);
                        return new GuestNotFoundException();
                    });
        } else {
            guestUser = createGuestUser();
        }

        TokenDataDto tokenData = jwtUtil.createTokenData(guestUser.getId());

        return LoginTokenDto.GuestLoginResponse.builder()
                .tokenData(tokenData)
                .userId(guestUser.getId())
                .providerId(guestUser.getProviderId())
                .isGuest(true)
                .build();
    }

    @Transactional
    public User upgradeGuestToGoogle(String guestProviderId, String googleProviderId,
                                     String email, String firstName, String lastName,
                                     String profileImage) {
        User guestUser = userRepository.findByProviderAndProviderIdWithLock(AuthProvider.GUEST, guestProviderId)
                .orElse(null);

        if (guestUser == null) {
            log.warn("Guest user not found with providerId: {}", guestProviderId);
            return null;
        }

        if (!AuthProvider.GUEST.equals(guestUser.getProvider())) {
            log.warn("Guest user already upgraded - guestProviderId: {}, current provider: {}", guestProviderId, guestUser.getProvider());
            return userRepository.save(guestUser.updateFromOAuth2(email, firstName, lastName, profileImage));
        }

        guestUser.upgradeToGoogleUser(
                AuthProvider.GOOGLE,
                googleProviderId,
                email,
                firstName,
                lastName,
                profileImage
        );

        User upgradedUser = userRepository.save(guestUser);

        return upgradedUser;
    }

    @Transactional(readOnly = true)
    public TokenDataDto refreshToken(String refreshToken) {
        return jwtUtil.refreshToken(refreshToken);
    }

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

    private void validateAuthKey(String key) {
        if (!authKey.equals(key)) {
            log.warn("Invalid authentication key attempt");
            throw new InvalidAuthKeyException();
        }
    }
}