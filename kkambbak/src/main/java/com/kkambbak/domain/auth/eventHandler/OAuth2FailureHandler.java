package com.kkambbak.domain.auth.eventHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.login-uri:https://kkam-bbak.pages.dev/login}")
    private String loginUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String targetUrl = loginUri;

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();

            if ("access_denied".equals(errorCode)) {
                request.getSession().setAttribute("oauth2_prompt", "consent");
                targetUrl = UriComponentsBuilder.fromUriString(loginUri)
                        .queryParam("error", "consent_cancelled")
                        .queryParam("retryConsent", "true")
                        .build().toUriString();
            }
        } else if (exception.getMessage() != null && exception.getMessage().contains("access_denied")) {
            request.getSession().setAttribute("oauth2_prompt", "consent");
            targetUrl = UriComponentsBuilder.fromUriString(loginUri)
                    .queryParam("error", "consent_cancelled")
                    .queryParam("retryConsent", "true")
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}