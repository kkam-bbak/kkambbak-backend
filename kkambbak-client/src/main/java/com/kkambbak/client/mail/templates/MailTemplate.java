package com.kkambbak.client.mail.templates;

import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Component
public class MailTemplate {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

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

    public String buildPaymentSuccessEmailBody(String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName) {
        String formattedDate = renewalDate.format(dateFormatter);
        return String.format("""
                Hi %s,

                Great news! Your payment has been successfully processed, and your Kkambbak subscription has been renewed. 🎉

                We're so glad to have you with us for another term.
                Thank you for trusting Kkambbak — we'll keep doing our best to make sure you never miss a thing 😉

                Payment Details

                Renewal Date: %s

                Amount: ₩%,d

                Payment Method: %s

                Here's to another great month together 💛

                Best,
                The Kkambbak Team
                """, userName, formattedDate, amount, paymentMethod);
    }

    public String buildSubscriptionExpiryReminderEmailBody(String userName, LocalDateTime expiryDate, String planName) {
        String formattedDate = expiryDate.format(dateFormatter);
        return String.format("""
                Hi %s,

                ⏰ Your payment is coming up in 2 days

                This is a quick reminder from Kkambbak — your next payment is scheduled for %s, just 2 days away.
                The payment will be automatically processed using your saved payment method.

                Payment Details

                Payment Date: %s

                Amount: N/A

                Payment Method: N/A

                Thank you for being with us.
                Best regards,
                The Kkambbak Service Team
                """, userName, formattedDate, formattedDate);
    }

    public String buildPaymentFailureEmailBody(String userName, LocalDateTime renewalDate, Long amount, String paymentMethod, String planName, String errorMessage) {
        String formattedDate = renewalDate.format(dateFormatter);
        return String.format("""
                Hi %s,

                We tried to renew your Kkambbak subscription, but your payment couldn't be completed.
                As a result, your subscription has now expired and your access to the service has been paused.

                Don't worry — you can easily restore your access by updating your payment method and completing the renewal.

                Payment Details

                Renewal Date: %s

                Amount: ₩%,d

                Payment Method: %s

                We'd love to have you back soon 💛
                If you need help, please reach us at kkambbak@gmail.com

                Best regards,
                The Kkambbak Team
                """, userName, formattedDate, amount, paymentMethod);
    }

    public String buildSubscriptionCancelledEmailBody(String userName, LocalDateTime cancelledDate, LocalDateTime endDate, String planName) {
        String cancelledFormatted = cancelledDate.format(dateFormatter);
        String endFormatted = endDate.format(dateFormatter);
        return String.format("""
                Hi %s,

                Your subscription has been cancelled as requested.

                Cancellation Details

                Plan: %s

                Cancelled Date: %s

                Current Period Ends: %s

                You will continue to have access to premium features until %s. After that, your account will revert to the standard plan.

                If you'd like to resubscribe in the future, we'd love to have you back!

                Best regards,
                The Kkambbak Team
                """, userName, planName, cancelledFormatted, endFormatted, endFormatted);
    }
}