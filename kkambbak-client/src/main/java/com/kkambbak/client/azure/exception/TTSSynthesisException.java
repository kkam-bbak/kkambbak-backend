package com.kkambbak.client.azure.exception;

import com.kkambbak.client.azure.enums.AzureErrorCode;

public class TTSSynthesisException extends TTSException {
    public TTSSynthesisException() {
        super(AzureErrorCode.TTS_FAIL);
    }

    public TTSSynthesisException(String message) {
        super(AzureErrorCode.TTS_FAIL.getCode(),message);
    }

}
