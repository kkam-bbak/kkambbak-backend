package com.kkambbak.core.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    STANDARD("일반 사용자"),
    PREMIUM("프리미엄 사용자");

    private final String description;
}