package com.kkambbak.domain.auth.eventHandler;

import com.kkambbak.client.mail.exception.EmailRateLimitExceededException;
import com.kkambbak.core.entity.user.EmailVerification;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.UserStatus;
import com.kkambbak.domain.auth.eventHandler.dto.GoogleOAuth2UserInfo;
import com.kkambbak.domain.auth.exception.OAuth2AuthenticationException;
import com.kkambbak.domain.auth.exception.UnsupportedOAuth2ProviderException;
import com.kkambbak.domain.auth.service.EmailService;
import com.kkambbak.domain.user.service.UserService;
import com.kkambbak.global.jwt.JwtUtil;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2EventHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${app.email.redirect-uri:http://localhost:3000/verify-email}")
    private String emailVerificationRedirectUri;

    @Value("${app.user.default-profile-image}")
    private String defaultProfileImage;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        String guestProviderId = (String) request.getSession().getAttribute("guestProviderId");

        try {
            User user = processOAuth2User(registrationId, oAuth2User, guestProviderId);
            String userEmail = user.getEmail();

            if (UserStatus.ACTIVE.equals(user.getStatus())) {
                TokenDataDto tokenData = jwtUtil.createTokenData(user.getId());

                String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                        .queryParam("accessToken", tokenData.getAccessToken())
                        .queryParam("refreshToken", tokenData.getRefreshToken())
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } else {
                try {
                    EmailVerification emailVerification = emailService.sendOtpEmail(userEmail, true);

                    String targetUrl = UriComponentsBuilder.fromUriString(emailVerificationRedirectUri)
                            .queryParam("code", emailVerification.getVerificationCode())
                            .build().toUriString();

                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                } catch (EmailRateLimitExceededException e) {
                    log.warn("Email rate limit exceeded for user: {}", userEmail);
                    String targetUrl = UriComponentsBuilder.fromUriString(emailVerificationRedirectUri)
                            .queryParam("error", "email_rate_limit_exceeded")
                            .build().toUriString();
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                }
            }

        } catch (Exception e) {
            log.error("Error in OAuth2 authentication success handler", e);
            throw new OAuth2AuthenticationException("OAuth2 인증 처리 중 오류 발생", e);
        }
    }

    private User processOAuth2User(String registrationId, OAuth2User oAuth2User, String guestProviderId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

            String profileImage = defaultProfileImage;

            if (guestProviderId != null && !guestProviderId.isEmpty()) {
                try {
                    User upgradedUser = userService.upgradeGuestToGoogle(
                            guestProviderId,
                            userInfo.getSocialId(),
                            userInfo.getEmail(),
                            userInfo.getName(),
                            profileImage
                    );

                    if (upgradedUser != null) {
                        return upgradedUser;
                    } else {
                        return userService.createOrUpdateUser(
                                "google",
                                userInfo.getSocialId(),
                                userInfo.getEmail(),
                                userInfo.getName(),
                                profileImage
                        );
                    }
                } catch (Exception e) {
                    return userService.createOrUpdateUser(
                            "google",
                            userInfo.getSocialId(),
                            userInfo.getEmail(),
                            userInfo.getName(),
                            profileImage
                    );
                }
            } else {
                return userService.createOrUpdateUser(
                        "google",
                        userInfo.getSocialId(),
                        userInfo.getEmail(),
                        userInfo.getName(),
                        profileImage
                );
            }
        } else {
            log.error("Unsupported OAuth2 provider: {}", registrationId);
            throw new UnsupportedOAuth2ProviderException(registrationId);
        }
    }
}