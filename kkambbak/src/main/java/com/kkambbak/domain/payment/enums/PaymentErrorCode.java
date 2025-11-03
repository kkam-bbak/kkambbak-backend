package com.kkambbak.domain.payment.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ResponseCode {

    PLAN_NOT_FOUND("PA001", "Subscription plan not found"),
    PAYMENT_NOT_FOUND("PA002", "Payment record not found"),
    UNAUTHORIZED_PAYMENT_ACCESS("PA003", "Unauthorized payment access"),
    PAYMENT_CREATION_FAILED("PA004", "Payment creation failed"),
    PAYMENT_CAPTURE_FAILED("PA005", "Payment capture failed"),
    INVALID_PAYMENT_STATUS("PA006", "Invalid payment status"),
    ALREADY_SUBSCRIBED("PA007", "Already subscribed"),
    PAYMENT_PENDING("PA008", "Payment is pending");

    private final String statusCode;
    private final String message;
}