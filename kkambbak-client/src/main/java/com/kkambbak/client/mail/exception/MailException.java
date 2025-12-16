package com.kkambbak.client.mail.exception;

import com.kkambbak.client.exception.ClientException;
import com.kkambbak.client.mail.enums.MailErrorCode;

public class MailException extends ClientException {

    public MailException(String code, String message) {
        super(code, message);
    }

    public MailException(MailErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}