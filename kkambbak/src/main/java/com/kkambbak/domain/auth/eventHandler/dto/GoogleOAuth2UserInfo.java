package com.kkambbak.domain.auth.eventHandler.dto;

import java.util.Map;

public class GoogleOAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getSocialId() {
        Object id = attributes.get("sub");
        return id != null ? id.toString() : null;
    }

    public String getEmail() {
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    public String getFirstName() {
        Object givenName = attributes.get("given_name");
        return givenName != null ? givenName.toString() : null;
    }

    public String getLastName() {
        Object familyName = attributes.get("family_name");
        return familyName != null ? familyName.toString() : null;
    }

    public String getProfileImage() {
        Object picture = attributes.get("picture");
        return picture != null ? picture.toString() : null;
    }
}