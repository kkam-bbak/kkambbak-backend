package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class SubscriptionNotFoundException extends CustomException {

    public SubscriptionNotFoundException() {
        super(PaymentErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    public SubscriptionNotFoundException(String message) {
        super(PaymentErrorCode.SUBSCRIPTION_NOT_FOUND, message);
    }
}