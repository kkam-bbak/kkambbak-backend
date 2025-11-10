package com.kkambbak.client.azure.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AzureErrorCode {
    PRONUNCIATION_FAIL("A001","발음 평가에 실패했습니다."),
    PRONUNCIATION_SERVICE_UNAVAILABLE("A002","발음 평가 서비스 접속에 실패했습니다."),
    TTS_FAIL("A003", "TTS에 실패했습니다.");

    private final String code;
    private final String message;
}
