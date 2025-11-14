package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class StandardCreditExceedException extends CustomException {
    public StandardCreditExceedException() {
        super(RoleplayErrorCode.ROLEPLAY_CREDIT_LIMIT_STANDARD);
    }

}
