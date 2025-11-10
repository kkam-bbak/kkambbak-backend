package com.kkambbak.client.mail.exception;

import com.kkambbak.client.mail.enums.MailErrorCode;

public class EmailRateLimitExceededException extends MailException {

    public EmailRateLimitExceededException() {
        super(MailErrorCode.EMAIL_RATE_LIMIT_EXCEEDED);
    }

    public EmailRateLimitExceededException(String message) {
        super(MailErrorCode.EMAIL_RATE_LIMIT_EXCEEDED.getCode(), message);
    }
}