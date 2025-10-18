package com.kkambbak.domain.auth.eventHandler;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.domain.auth.eventHandler.dto.GoogleOAuth2UserInfo;
import com.kkambbak.domain.user.service.UserService;
import com.kkambbak.global.jwt.JwtUtil;
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

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

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

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } finally {
            request.getSession().removeAttribute("guestProviderId");
        }
    }

    private User processOAuth2User(String registrationId, OAuth2User oAuth2User, String guestProviderId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

            if (guestProviderId != null && !guestProviderId.isEmpty()) {
                try {
                    User upgradedUser = userService.upgradeGuestToGoogle(
                            guestProviderId,
                            userInfo.getSocialId(),
                            userInfo.getEmail(),
                            userInfo.getFirstName(),
                            userInfo.getLastName(),
                            userInfo.getProfileImage()
                    );

                    if (upgradedUser != null) {
                        return upgradedUser;
                    } else {
                        return userService.createOrUpdateUser(
                                "google",
                                userInfo.getSocialId(),
                                userInfo.getEmail(),
                                userInfo.getFirstName(),
                                userInfo.getLastName(),
                                userInfo.getProfileImage()
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to upgrade guest to Google - guestProviderId: {}, error: {}", guestProviderId, e.getMessage(), e);
                    return userService.createOrUpdateUser(
                            "google",
                            userInfo.getSocialId(),
                            userInfo.getEmail(),
                            userInfo.getFirstName(),
                            userInfo.getLastName(),
                            userInfo.getProfileImage()
                    );
                }
            } else {
                return userService.createOrUpdateUser(
                        "google",
                        userInfo.getSocialId(),
                        userInfo.getEmail(),
                        userInfo.getFirstName(),
                        userInfo.getLastName(),
                        userInfo.getProfileImage()
                );
            }
        } else {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
    }
}