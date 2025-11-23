package com.kkambbak.domain.auth.constant;

public class OAuth2Constants {
    public static final String SESSION_OAUTH2_PROMPT = "oauth2_prompt";
    public static final String PROMPT_CONSENT = "consent";
    public static final String GOOGLE_PROVIDER = "google";
    public static final int PROMPT_TTL_MINUTES = 10;

    private OAuth2Constants() {
        throw new UnsupportedOperationException("Utility class");
    }
}