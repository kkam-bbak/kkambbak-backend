package com.kkambbak.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class OAuth2Config {

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
                        String prompt = httpRequest.getParameter("prompt");

                        if (guestProviderId != null && !guestProviderId.isEmpty()) {
                            httpRequest.getSession().setAttribute("guestProviderId", guestProviderId);
                        }
                        if (prompt != null) {
                            httpRequest.getSession().setAttribute("oauth2_prompt", prompt);
                        }
                    }
                })
                .additionalParameters(params -> {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();
                        String prompt = (String) httpRequest.getSession().getAttribute("oauth2_prompt");

                        if ("consent".equals(prompt)) {
                            params.put("prompt", "consent");
                            httpRequest.getSession().removeAttribute("oauth2_prompt");
                        }
                    }
                    params.put("access_type", "offline");
                })
        );

        return resolver;
    }
}