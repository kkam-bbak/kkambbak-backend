package com.kkambbak.client.openai.exception;

import com.kkambbak.client.exception.ClientException;
import com.kkambbak.client.mail.enums.MailErrorCode;
import com.kkambbak.client.openai.enums.OpenAiErrorCode;

public class OpenAiException extends ClientException {
    public OpenAiException(String code, String message) {
        super(code, message);
    }
    public OpenAiException(OpenAiErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}
