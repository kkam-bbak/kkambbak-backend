package com.kkambbak.domain.roleplay.exception;

import com.kkambbak.domain.roleplay.enums.RoleplayErrorCode;
import com.kkambbak.global.exception.CustomException;

public class FFmpegConvertFailException extends CustomException {
    public FFmpegConvertFailException() {
        super(RoleplayErrorCode.ROLEPLAY_AUDIO_FFMPEG_CONVERT_FAIL);
    }
}
