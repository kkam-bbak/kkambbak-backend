package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class PaymentCaptureFailedException extends CustomException {

    public PaymentCaptureFailedException() {
        super(PaymentErrorCode.PAYMENT_CAPTURE_FAILED);
    }

    public PaymentCaptureFailedException(String message) {
        super(PaymentErrorCode.PAYMENT_CAPTURE_FAILED, message);
    }
}