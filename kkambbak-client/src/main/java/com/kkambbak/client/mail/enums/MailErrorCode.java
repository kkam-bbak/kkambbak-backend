package com.kkambbak.client.mail.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailErrorCode {
    EMAIL_RATE_LIMIT_EXCEEDED("MI001", "이메일 발송 횟수를 초과했습니다. 1시간에 최대 30개의 이메일을 발송할 수 있습니다."),
    OTP_EMAIL_SEND_FAILED("MI002", "OTP 이메일 발송에 실패했습니다."),
    PAYMENT_SUCCESS_EMAIL_SEND_FAILED("MI003", "결제 성공 이메일 발송에 실패했습니다."),
    PAYMENT_FAILURE_EMAIL_SEND_FAILED("MI004", "결제 실패 이메일 발송에 실패했습니다."),
    SUBSCRIPTION_EXPIRY_REMINDER_EMAIL_SEND_FAILED("MI005", "구독 만료 알림 이메일 발송에 실패했습니다."),
    SUBSCRIPTION_CANCELLED_EMAIL_SEND_FAILED("MI006", "구독 취소 이메일 발송에 실패했습니다."),
    INVALID_EMAIL_ADDRESS("MI007", "유효하지 않은 이메일 주소입니다."),
    MAIL_SERVER_ERROR("MI008", "메일 서버 오류가 발생했습니다.");

    private final String code;
    private final String message;
}