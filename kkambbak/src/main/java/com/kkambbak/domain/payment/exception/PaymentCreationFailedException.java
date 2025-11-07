package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class PaymentCreationFailedException extends CustomException {

    public PaymentCreationFailedException() {
        super(PaymentErrorCode.PAYMENT_CREATION_FAILED);
    }

    public PaymentCreationFailedException(String message) {
        super(PaymentErrorCode.PAYMENT_CREATION_FAILED, message);
    }
}