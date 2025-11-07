package com.kkambbak.domain.user.service;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.core.entity.user.enums.UserStatus;
import com.kkambbak.core.entity.user.enums.UserRole;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.user.dto.LoginTokenDto;
import com.kkambbak.domain.user.dto.UpdateProfileDto;
import com.kkambbak.domain.user.dto.GetProfileDto;
import com.kkambbak.domain.user.exception.InvalidAuthKeyException;
import com.kkambbak.domain.user.exception.LogoutFailedException;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import com.kkambbak.domain.user.exception.GuestNotFoundException;
import com.kkambbak.domain.user.exception.InvalidGuestIdException;
import com.kkambbak.domain.user.exception.ProfileValidationException;
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

    @Value("${app.user.default-profile-image}")
    private String defaultProfileImage;

    @Transactional
    public User createOrUpdateUser(String provider, String providerId, String email,
                                   String name, String profileImage) {
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());

        return userRepository.findByProviderAndProviderId(authProvider, providerId)
                .map(existingUser -> {
                    return userRepository.save(
                            existingUser.updateFromOAuth2(email, name, profileImage)
                    );
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
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
        String guestName = "Guest_" + providerId.substring(Math.max(0, providerId.length() - 8));

        User guestUser = User.builder()
                .name(guestName)
                .email(null)
                .profileImage(defaultProfileImage)
                .provider(AuthProvider.GUEST)
                .providerId(providerId)
                .isGuest(true)
                .status(UserStatus.ACTIVE)
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
                .providerId(guestUser.getProviderId())
                .isGuest(true)
                .build();
    }

    @Transactional
    public User upgradeGuestToGoogle(String guestProviderId, String googleProviderId,
                                     String email, String name, String profileImage) {
        User guestUser = userRepository.findByProviderAndProviderIdWithLock(AuthProvider.GUEST, guestProviderId)
                .orElse(null);

        if (guestUser == null) {
            log.warn("Guest user not found with providerId: {}", guestProviderId);
            return null;
        }

        if (!AuthProvider.GUEST.equals(guestUser.getProvider())) {
            log.warn("Guest user already upgraded - guestProviderId: {}, current provider: {}", guestProviderId, guestUser.getProvider());
            return userRepository.save(guestUser.updateFromOAuth2(email, name, profileImage));
        }

        guestUser.upgradeToGoogleUser(
                AuthProvider.GOOGLE,
                googleProviderId,
                email,
                name,
                profileImage
        );

        return userRepository.save(guestUser);
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

    public void register(Long userId, UpdateProfileDto request, Gender gender) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateProfile(
                request.getName(),
                gender,
                request.getCountryOfOrigin(),
                request.getPersonalityOrImage(),
                request.getPreferredNameMeaning()
        );

        userRepository.save(user);
    }

    public Gender validateProfileRequest(UpdateProfileDto request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ProfileValidationException("이름은 필수입니다");
        }
        if (request.getName().length() > 200) {
            throw new ProfileValidationException("이름은 200자 이하여야 합니다");
        }

        if (request.getCountryOfOrigin() == null || request.getCountryOfOrigin().isBlank()) {
            throw new ProfileValidationException("국가는 필수입니다");
        }
        if (request.getCountryOfOrigin().length() > 100) {
            throw new ProfileValidationException("국가는 100자 이하여야 합니다");
        }

        if (request.getPersonalityOrImage() == null || request.getPersonalityOrImage().isBlank()) {
            throw new ProfileValidationException("Personality, Image는 필수입니다");
        }

        if (request.getPreferredNameMeaning() == null || request.getPreferredNameMeaning().isBlank()) {
            throw new ProfileValidationException("선호하는 이름 의미는 필수입니다");
        }

        if (request.getGender() == null || request.getGender().isBlank()) {
            throw new ProfileValidationException("성별은 필수입니다");
        }

        try {
            return Gender.valueOf(request.getGender().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid gender value: {}", request.getGender());
            throw new ProfileValidationException("유효하지 않은 성별입니다. (MALE, FEMALE만 가능)");
        }
    }

    @Transactional(readOnly = true)
    public GetProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return GetProfileDto.from(user);
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

    @Transactional
    public void upgradeRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getRole() != UserRole.PREMIUM) {
            user.setRole(UserRole.PREMIUM);
            userRepository.save(user);
        }
    }

    @Transactional
    public void downgradeRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getRole() != UserRole.STANDARD) {
            user.setRole(UserRole.STANDARD);
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateAuthKey(String key) {
        if (!authKey.equals(key)) {
            log.warn("Invalid authentication key attempt");
            throw new InvalidAuthKeyException();
        }
    }
}