package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 수단
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    KAKAO("카카오페이");

    private final String description;
}