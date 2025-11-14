package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;


public class NoContentInSessionException extends CustomException {
    public NoContentInSessionException() {
        super(RoleplayErrorCode.ROLEPLAY_SESSION_NO_CONTENT);
    }
}
