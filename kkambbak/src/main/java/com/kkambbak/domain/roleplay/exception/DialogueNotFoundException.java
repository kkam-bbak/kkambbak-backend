package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class DialogueNotFoundException extends CustomException {
    public DialogueNotFoundException() {
        super(RoleplayErrorCode.ROLEPLAY_DIALOGUE_NOT_FOUND);
    }
}
