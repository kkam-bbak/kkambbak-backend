package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class ScenarioNotFoundException extends CustomException {
    public ScenarioNotFoundException() {super(RoleplayErrorCode.ROLEPLAY_SCENARIO_NOT_FOUND);
    }
}
