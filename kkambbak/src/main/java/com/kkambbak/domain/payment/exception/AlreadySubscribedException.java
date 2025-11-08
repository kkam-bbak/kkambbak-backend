package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class AlreadySubscribedException extends CustomException {

    public AlreadySubscribedException() {
        super(PaymentErrorCode.ALREADY_SUBSCRIBED);
    }

    public AlreadySubscribedException(String message) {
        super(PaymentErrorCode.ALREADY_SUBSCRIBED, message);
    }
}