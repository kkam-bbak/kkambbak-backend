package com.kkambbak.domain.user.exception;

import com.kkambbak.core.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class InvalidGuestIdException extends CustomException {

    public InvalidGuestIdException() {
        super(UserErrorCode.INVALID_GUEST_ID);
    }
}