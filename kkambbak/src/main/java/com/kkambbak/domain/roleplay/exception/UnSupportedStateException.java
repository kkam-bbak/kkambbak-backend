package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class UnSupportedStateException extends CustomException {
    public UnSupportedStateException( ) {
        super(RoleplayErrorCode.UNSUPPORTED_SESSION_STATE);
    }
}
