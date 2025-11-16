package com.kkambbak.domain.name.exception;

import com.kkambbak.domain.name.enums.NameErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NameHistoryNotFoundException extends CustomException {
    public NameHistoryNotFoundException() {
        super(NameErrorCode.NAME_HISTORY_NOT_FOUND);
    }
}
