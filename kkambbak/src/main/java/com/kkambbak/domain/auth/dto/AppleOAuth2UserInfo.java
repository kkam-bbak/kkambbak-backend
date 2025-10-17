package com.kkambbak.domain.auth.dto;

import java.util.Map;

public class AppleOAuth2UserInfo {

    private final Map<String, Object> attributes;

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
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

    @SuppressWarnings("unchecked")
    public String getFirstName() {
        Map<String, Object> name = (Map<String, Object>) attributes.get("name");
        if (name != null) {
            Object firstName = name.get("firstName");
            return firstName != null ? firstName.toString() : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getLastName() {
        Map<String, Object> name = (Map<String, Object>) attributes.get("name");
        if (name != null) {
            Object lastName = name.get("lastName");
            return lastName != null ? lastName.toString() : null;
        }
        return null;
    }

    public String getProfileImage() {
        // Apple does not provide profile image
        return null;
    }
}