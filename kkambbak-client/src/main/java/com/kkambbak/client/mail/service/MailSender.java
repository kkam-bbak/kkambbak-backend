package com.kkambbak.client.mail.service;

public interface MailSender {
    void sendOtpEmail(String toEmail, String otpCode);
}