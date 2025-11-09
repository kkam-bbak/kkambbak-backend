package com.kkambbak.client.mail.service;

import com.kkambbak.client.mail.templates.MailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailSender {

    private final JavaMailSender javaMailSender;
    private final MailTemplate mailTemplate;
    private final EmailRateLimiter emailRateLimiter;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        emailRateLimiter.checkAndIncrementEmailCount(toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("깜빡 인증 코드입니다");
        message.setText(mailTemplate.buildOtpEmailBody(otpCode));

        javaMailSender.send(message);
    }

    @Override
    @Async
    public void sendPaymentSuccessEmail(String toEmail, String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🎉 Payment Successful – Your subscription has been renewed!");
            message.setText(mailTemplate.buildPaymentSuccessEmailBody(userName, renewalDate, amount, paymentMethod, planName));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send payment success email to {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendSubscriptionExpiryReminderEmail(String toEmail, String userName, LocalDateTime expiryDate, String planName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⏰ Your payment is coming up in 2 days");
            message.setText(mailTemplate.buildSubscriptionExpiryReminderEmailBody(userName, expiryDate, planName));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send subscription expiry reminder email to {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendPaymentFailureEmail(String toEmail, String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName, String errorMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Subscription Expired – Payment couldn't be processed");
            message.setText(mailTemplate.buildPaymentFailureEmailBody(userName, renewalDate, amount, paymentMethod, planName, errorMessage));
            javaMailSender.send(message);
            log.info("Payment failure email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send payment failure email to {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendSubscriptionCancelledEmail(String toEmail, String userName, LocalDateTime cancelledDate, LocalDateTime endDate, String planName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Your Kkambbak subscription has been cancelled");
            message.setText(mailTemplate.buildSubscriptionCancelledEmailBody(userName, cancelledDate, endDate, planName));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send subscription cancelled email to {}", toEmail, e);
        }
    }
}