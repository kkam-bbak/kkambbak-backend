package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class GPTResponseTypeException extends CustomException {
    public GPTResponseTypeException() {super(RoleplayErrorCode.ROLEPLAY_GPT_ANSWER_TYPE_IS_NOT_JSON);
    }
}
