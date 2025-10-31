package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 구독 상태
 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    ACTIVE("활성화"),
    EXPIRED("만료"),
    CANCELLED("취소"),
    PENDING("대기중");

    private final String description;
}