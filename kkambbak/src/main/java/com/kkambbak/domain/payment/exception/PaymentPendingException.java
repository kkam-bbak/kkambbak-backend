package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class PaymentPendingException extends CustomException {

    public PaymentPendingException() {
        super(PaymentErrorCode.PAYMENT_PENDING);
    }

    public PaymentPendingException(String message) {
        super(PaymentErrorCode.PAYMENT_PENDING, message);
    }
}