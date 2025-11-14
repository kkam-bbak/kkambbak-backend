package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class GPTConnectFailException extends CustomException {
    public GPTConnectFailException() {super(RoleplayErrorCode.ROLEPLAY_FAIL_TO_GET_ANSWER_FROM_GPT);
    }
}
