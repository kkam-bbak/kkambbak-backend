package com.kkambbak.client.azure.exception;

import com.kkambbak.client.azure.enums.AzureErrorCode;

public class PronunciationUnavailableException extends PronunciationException {
    public PronunciationUnavailableException() {
        super(AzureErrorCode.PRONUNCIATION_SERVICE_UNAVAILABLE);
    }
}
