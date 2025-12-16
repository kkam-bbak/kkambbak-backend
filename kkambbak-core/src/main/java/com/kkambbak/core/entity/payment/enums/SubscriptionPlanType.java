package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlanType {
    STANDARD("무료"),
    PREMIUM("프리미엄");

    private final String description;
}