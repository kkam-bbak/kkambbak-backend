package com.kkambbak.domain.user.exception;

import com.kkambbak.core.exception.CustomException;
import com.kkambbak.domain.user.enums.UserErrorCode;

public class GuestNotFoundException extends CustomException {

    public GuestNotFoundException() {
        super(UserErrorCode.GUEST_NOT_FOUND);
    }
}