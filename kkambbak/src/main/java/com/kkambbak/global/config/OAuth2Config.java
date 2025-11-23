package com.kkambbak.global.config;

import com.kkambbak.domain.auth.constant.OAuth2Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

    private final StringRedisTemplate redisTemplate;

    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization"
                );

        resolver.setAuthorizationRequestCustomizer(request ->
                request.attributes(attrs -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();
                        String guestProviderId = httpRequest.getParameter("guestProviderId");

                        if (guestProviderId != null && !guestProviderId.isEmpty()) {
                            httpRequest.getSession().setAttribute("guestProviderId", guestProviderId);
                        }
                    }
                })
                .additionalParameters(params -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();
                        String sessionId = httpRequest.getSession().getId();
                        String redisKey = OAuth2Constants.REDIS_KEY_PREFIX + sessionId;

                        // Redis에서 oauth2_prompt 확인 (FailureHandler에서 설정됨)
                        String prompt = redisTemplate.opsForValue().get(redisKey);
                        if (OAuth2Constants.PROMPT_CONSENT.equals(prompt)) {
                            params.put("prompt", OAuth2Constants.PROMPT_CONSENT);
                            redisTemplate.delete(redisKey);  // 사용 후 삭제
                        }

                        // Google 전용: Refresh Token 요청
                        if (isGoogleProvider(httpRequest)) {
                            params.put("access_type", "offline");
                        }
                    }
                })
        );

        return resolver;
    }

    private boolean isGoogleProvider(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri != null && requestUri.equals("/oauth2/authorization/google");
    }
}