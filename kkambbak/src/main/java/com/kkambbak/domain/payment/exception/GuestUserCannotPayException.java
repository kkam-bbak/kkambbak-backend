package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class GuestUserCannotPayException extends CustomException {

    public GuestUserCannotPayException() {
        super(PaymentErrorCode.GUEST_USER_CANNOT_PAY);
    }

    public GuestUserCannotPayException(String message) {
        super(PaymentErrorCode.GUEST_USER_CANNOT_PAY, message);
    }
}