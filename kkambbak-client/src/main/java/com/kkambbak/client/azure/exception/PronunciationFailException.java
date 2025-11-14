package com.kkambbak.client.azure.exception;

import com.kkambbak.client.azure.enums.AzureErrorCode;

public class PronunciationFailException extends PronunciationException {
    public PronunciationFailException() {
        super(AzureErrorCode.PRONUNCIATION_FAIL);
    }
}
