package com.kkambbak.client.azure.exception;

import com.kkambbak.client.azure.enums.AzureErrorCode;
import com.kkambbak.client.exception.ClientException;

public class PronunciationException extends ClientException {
    public PronunciationException(String code, String message) {
        super(code, message);
    }

    public PronunciationException(AzureErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}
