package com.kkambbak.client.mail.templates;

import org.springframework.stereotype.Component;

@Component
public class MailTemplate {

    public String buildOtpEmailBody(String otpCode) {
        return String.format("""
                안녕하세요!

                깜빡 인증을 위한 코드입니다.

                [인증 코드]
                %s

                이 코드는 5분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.

                감사합니다.
                깜빡 팀
                """, otpCode);
    }
}