package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class NoAnswerFromGPTException extends CustomException {

    public NoAnswerFromGPTException() {
        super(RoleplayErrorCode.ROLEPLAY_NO_ANSWER);
    }
}
