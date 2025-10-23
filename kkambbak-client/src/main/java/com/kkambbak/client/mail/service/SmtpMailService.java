package com.kkambbak.client.mail.service;

import com.kkambbak.client.mail.templates.MailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailSender {

    private final JavaMailSender javaMailSender;
    private final MailTemplate mailTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("깜빡 인증 코드입니다");
        message.setText(mailTemplate.buildOtpEmailBody(otpCode));

        javaMailSender.send(message);
    }
}