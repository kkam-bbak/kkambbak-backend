package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 구독 플랜 타입
 */
@Getter
@RequiredArgsConstructor
public enum SubscriptionPlanType {
    STANDARD("무료"),
    PREMIUM("프리미엄");

    private final String description;
}