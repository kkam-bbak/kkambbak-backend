package com.kkambbak.core.entity.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 상태
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("대기중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}