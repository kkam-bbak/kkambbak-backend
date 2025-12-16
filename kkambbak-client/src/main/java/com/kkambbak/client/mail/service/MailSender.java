package com.kkambbak.client.mail.service;

import java.time.LocalDateTime;

public interface MailSender {
    void sendOtpEmail(String toEmail, String otpCode);

    // 결제, 갱신 성공
    void sendPaymentSuccessEmail(String toEmail, String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName);

    // 구독 종료 예정 (2일 전)
    void sendSubscriptionExpiryReminderEmail(String toEmail, String userName, LocalDateTime expiryDate, String planName);

    // 결제, 갱신 실패
    void sendPaymentFailureEmail(String toEmail, String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName, String errorMessage);

    // 구독 취소 완료
    void sendSubscriptionCancelledEmail(String toEmail, String userName, LocalDateTime cancelledDate, LocalDateTime endDate, String planName);
}