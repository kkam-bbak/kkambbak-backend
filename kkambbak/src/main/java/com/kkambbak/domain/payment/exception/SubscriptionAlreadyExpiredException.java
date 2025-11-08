package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class SubscriptionAlreadyExpiredException extends CustomException {

    public SubscriptionAlreadyExpiredException() {
        super(PaymentErrorCode.SUBSCRIPTION_ALREADY_EXPIRED);
    }

    public SubscriptionAlreadyExpiredException(String message) {
        super(PaymentErrorCode.SUBSCRIPTION_ALREADY_EXPIRED, message);
    }
}