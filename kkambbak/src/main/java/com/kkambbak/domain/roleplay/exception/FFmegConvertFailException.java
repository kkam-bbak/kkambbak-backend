package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.code.ResponseCode;
import com.kkambbak.global.exception.CustomException;

public class FFmegConvertFailException extends CustomException {
    public FFmegConvertFailException() {
        super(RoleplayErrorCode.ROLEPLAY_AUDIO_FFMEG_CONVERT_FAIL);
    }
}
