package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class PremiumCreditExceedException extends CustomException {
    public PremiumCreditExceedException() {
        super(RoleplayErrorCode.ROLEPLAY_CREDIT_LIMIT_PREMIUM);
    }
}
