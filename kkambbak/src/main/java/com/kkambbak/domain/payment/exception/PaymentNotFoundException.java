package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class PaymentNotFoundException extends CustomException {

    public PaymentNotFoundException() {
        super(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    public PaymentNotFoundException(String message) {
        super(PaymentErrorCode.PAYMENT_NOT_FOUND, message);
    }
}