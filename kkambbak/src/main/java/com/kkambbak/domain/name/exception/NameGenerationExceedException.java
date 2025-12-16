package com.kkambbak.domain.name.exception;

import com.kkambbak.domain.name.enums.NameErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NameGenerationExceedException extends CustomException {
    public NameGenerationExceedException() {
        super(NameErrorCode.NAME_GENERATION_CHANCE_LIMIT);
    }
}
