package com.kkambbak.domain.name.enums;


import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NameErrorCode implements ResponseCode {
    NAME_GENERATION_CHANCE_LIMIT("N001","Name Generation Chance approach Limit"),
    NAME_GENERATION_JSON_PARSE_FAIL("N002", "Failed to parse AI-generated name JSON"),
    NAME_GENERATION_SAVE_FAIL("N003", "Failed to save generated name result"),;

    private final String statusCode;
    private final String message;
}
