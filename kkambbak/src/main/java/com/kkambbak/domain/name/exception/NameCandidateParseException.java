package com.kkambbak.domain.name.exception;

import com.kkambbak.domain.name.enums.NameErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NameCandidateParseException extends CustomException {
    public NameCandidateParseException() {
        super(NameErrorCode.NAME_GENERATION_JSON_PARSE_FAIL);
    }
}
