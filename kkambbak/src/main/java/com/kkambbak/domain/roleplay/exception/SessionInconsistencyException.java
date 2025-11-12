package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class SessionInconsistencyException extends CustomException {
    public SessionInconsistencyException() {
        super(RoleplayErrorCode.ROLEPLAY_SESSION_AND_USER_ARE_NOT_SAME);
    }
}
