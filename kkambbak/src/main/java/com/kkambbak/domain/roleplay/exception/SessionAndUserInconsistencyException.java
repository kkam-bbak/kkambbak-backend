package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class SessionAndUserInconsistencyException extends CustomException {
    public SessionAndUserInconsistencyException() {
        super(RoleplayErrorCode.ROLEPLAY_SESSION_AND_USER_ARE_NOT_SAME);
    }
}
