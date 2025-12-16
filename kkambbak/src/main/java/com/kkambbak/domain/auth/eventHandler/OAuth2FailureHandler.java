package com.kkambbak.domain.auth.eventHandler;

import com.kkambbak.domain.auth.constant.OAuth2Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.login-uri:https://kkam-bbak.pages.dev/login}")
    private String loginUri;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String targetUrl = loginUri;

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            var error = oauth2Exception.getError();
            if (error != null) {
                String errorCode = error.getErrorCode();
                log.warn("OAuth2 authentication failed with error code: {}", errorCode);

                if (OAuth2Constants.ERROR_ACCESS_DENIED.equals(errorCode)) {
                    log.info("User cancelled OAuth2 consent, setting retry flag in Redis");
                    String sessionId = request.getSession().getId();
                    String redisKey = OAuth2Constants.REDIS_KEY_PREFIX + sessionId;

                    redisTemplate.opsForValue().set(
                        redisKey,
                        OAuth2Constants.PROMPT_CONSENT,
                        OAuth2Constants.PROMPT_TTL_MINUTES,
                        TimeUnit.MINUTES
                    );

                    targetUrl = UriComponentsBuilder.fromUriString(loginUri)
                            .queryParam("error", "consent_cancelled")
                            .queryParam("retryConsent", "true")
                            .build().toUriString();
                }
            } else {
                log.error("OAuth2 error object is null");
            }
        } else {
            log.error("OAuth2 authentication failed: {}", exception.getMessage());
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}