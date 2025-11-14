package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class DialogueLimitExceedException extends CustomException {
    public DialogueLimitExceedException() {
        super(RoleplayErrorCode.ROLEPLAY_DIALOGUE_LIMIT);
    }
}
