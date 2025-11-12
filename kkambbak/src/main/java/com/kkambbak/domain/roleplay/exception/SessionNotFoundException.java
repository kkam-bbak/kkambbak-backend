package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class SessionNotFoundException extends CustomException {
    public SessionNotFoundException() {
        super(RoleplayErrorCode.ROLEPLAY_SESSION_NOT_FOUND);
    }
}
