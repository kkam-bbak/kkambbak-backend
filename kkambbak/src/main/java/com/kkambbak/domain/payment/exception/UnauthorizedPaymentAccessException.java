package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class UnauthorizedPaymentAccessException extends CustomException {

    public UnauthorizedPaymentAccessException() {
        super(PaymentErrorCode.UNAUTHORIZED_PAYMENT_ACCESS);
    }

    public UnauthorizedPaymentAccessException(String message) {
        super(PaymentErrorCode.UNAUTHORIZED_PAYMENT_ACCESS, message);
    }
}