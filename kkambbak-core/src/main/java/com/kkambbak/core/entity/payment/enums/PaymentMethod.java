package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    KAKAO("Kakao Pay");

    private final String description;
}