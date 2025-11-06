package com.kkambbak.domain.learning.exception;

import com.kkambbak.domain.learning.enums.LearningErrorCode;
import com.kkambbak.global.exception.CustomException;

/**
 * 상위 노출 규칙과 세션 정보가 불일치할 때 발생
 */
public class InconsistentExposureRuleException extends CustomException {
    public InconsistentExposureRuleException() {
        super(LearningErrorCode.INCONSISTENT_EXPOSURE_RULE);
    }
}