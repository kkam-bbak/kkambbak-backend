package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class TTSUploadFailException extends CustomException {
    public TTSUploadFailException() {
        super(RoleplayErrorCode.ROLEPLAY_TTS_UPLOAD_FAILED);
    }
}
