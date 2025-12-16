package com.kkambbak.core.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OtpStatus {
    PENDING("대기중"),
    VERIFIED("검증완료"),
    EXPIRED("만료");

    private final String description;
}