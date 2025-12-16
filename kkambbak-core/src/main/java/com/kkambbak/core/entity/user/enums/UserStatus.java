package com.kkambbak.core.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성화"),
    PENDING("대기중"),
    DELETED("탈퇴");

    private final String description;
}