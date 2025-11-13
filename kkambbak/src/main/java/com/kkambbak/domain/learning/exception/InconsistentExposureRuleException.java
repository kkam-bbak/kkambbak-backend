package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

public class InconsistentExposureRuleException extends CustomException {
    public InconsistentExposureRuleException() {
        super(LearningErrorCode.INCONSISTENT_EXPOSURE_RULE);
    }
}