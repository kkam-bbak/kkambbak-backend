package com.kkambbak.domain.name.exception;

import com.kkambbak.domain.name.enums.NameErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NameGenerationSaveException extends CustomException {
    public NameGenerationSaveException() {
        super(NameErrorCode.NAME_GENERATION_SAVE_FAIL);
    }
}
