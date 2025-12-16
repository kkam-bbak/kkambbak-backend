package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class InvalidPaymentStatusException extends CustomException {

    public InvalidPaymentStatusException() {
        super(PaymentErrorCode.INVALID_PAYMENT_STATUS);
    }

    public InvalidPaymentStatusException(String message) {
        super(PaymentErrorCode.INVALID_PAYMENT_STATUS, message);
    }
}