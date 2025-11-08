package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class SubscriptionAlreadyCancelledException extends CustomException {

    public SubscriptionAlreadyCancelledException() {
        super(PaymentErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
    }

    public SubscriptionAlreadyCancelledException(String message) {
        super(PaymentErrorCode.SUBSCRIPTION_ALREADY_CANCELLED, message);
    }
}