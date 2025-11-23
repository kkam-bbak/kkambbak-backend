package com.kkambbak.domain.auth.constant;

public class OAuth2Constants {
    public static final String PROMPT_CONSENT = "consent";
    public static final String ERROR_ACCESS_DENIED = "access_denied";
    public static final String REDIS_KEY_PREFIX = "oauth2:prompt:";
    public static final int PROMPT_TTL_MINUTES = 10;

    private OAuth2Constants() {
        throw new UnsupportedOperationException("Utility class");
    }
}