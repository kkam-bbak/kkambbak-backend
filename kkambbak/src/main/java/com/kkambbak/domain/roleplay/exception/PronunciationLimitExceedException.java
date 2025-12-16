package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class PronunciationLimitExceedException extends CustomException {
    public PronunciationLimitExceedException() {
        super(RoleplayErrorCode.ROLEPLAY_PRONUNCIATION_CHANCE_LIMIT);
    }
}
