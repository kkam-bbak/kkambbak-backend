package com.kkambbak.client.azure.exception;

import com.kkambbak.client.azure.enums.AzureErrorCode;
import com.kkambbak.client.exception.ClientException;

public class TTSException extends ClientException {

    public TTSException(String code, String message) {
        super(code, message);
    }

    public TTSException(AzureErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

}
