package com.kkambbak.domain.name.exception;

import com.kkambbak.domain.name.enums.NameErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NameHistoryForbiddenException extends CustomException {
    public NameHistoryForbiddenException() {
        super(NameErrorCode.NAME_HISTORY_FORBIDDEN);
    }
}
