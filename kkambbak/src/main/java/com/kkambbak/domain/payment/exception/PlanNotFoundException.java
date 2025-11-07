package com.kkambbak.domain.payment.exception;

import com.kkambbak.global.exception.CustomException;
import com.kkambbak.domain.payment.enums.PaymentErrorCode;

public class PlanNotFoundException extends CustomException {

    public PlanNotFoundException() {
        super(PaymentErrorCode.PLAN_NOT_FOUND);
    }

    public PlanNotFoundException(String message) {
        super(PaymentErrorCode.PLAN_NOT_FOUND, message);
    }
}