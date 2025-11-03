package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class InvalidPageRequestException extends CustomException {

    public InvalidPageRequestException() {
        super(PaymentErrorCode.INVALID_PAGE_REQUEST);
    }

    public InvalidPageRequestException(String message) {
        super(PaymentErrorCode.INVALID_PAGE_REQUEST, message);
    }
}