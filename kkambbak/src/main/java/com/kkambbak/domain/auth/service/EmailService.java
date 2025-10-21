package com.kkambbak.domain.auth.service;

import com.kkambbak.client.mail.service.MailSender;
import com.kkambbak.core.entity.user.EmailVerification;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.user.EmailVerificationRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.auth.exception.UserNotFoundException;
import com.kkambbak.domain.auth.exception.OtpVerificationFailedException;
import com.kkambbak.domain.auth.exception.OtpCodeExpiredException;
import com.kkambbak.domain.auth.exception.OtpCodeInvalidException;
import com.kkambbak.domain.auth.exception.OtpConcurrencyException;
import com.kkambbak.domain.auth.exception.OtpSendFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final MailSender mailSender;

    @Value("${app.email.otp-expiry-minutes:5}")
    private int otpExpiryMinutes;

    private static final Random random = new Random();
    // 메일 전송 시 예외가 발생했을 떄 최대 3번까지 전송 재시도
    private static final int MAX_RETRY = 3;

    public String generateOtpCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EmailVerification sendOtpEmail(String email) {
        return retryWithBackoff(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(UserNotFoundException::new);

            java.util.List<EmailVerification> unverifiedOtps =
                    emailVerificationRepository.findAllUnverifiedByUserId(user.getId());
            for (EmailVerification otp : unverifiedOtps) {
                otp.expireOtp();
                emailVerificationRepository.save(otp);
            }
            if (!unverifiedOtps.isEmpty()) {
                log.info("Expired {} unverified OTPs for userId: {}", unverifiedOtps.size(), user.getId());
            }

            String otpCode = generateOtpCode();
            String verificationCode = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

            EmailVerification newVerification = EmailVerification.builder()
                    .userId(user.getId())
                    .otpCode(otpCode)
                    .expiresAt(expiresAt)
                    .verificationCode(verificationCode)
                    .build();
            newVerification = emailVerificationRepository.save(newVerification);
            sendEmail(email, otpCode);

            return newVerification;
        }, "OTP 발송");
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean verifyOtp(String email, String otpCode) {
        return retryWithBackoff(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(UserNotFoundException::new);

            EmailVerification verification = emailVerificationRepository
                    .findLatestUnverifiedByUserId(user.getId())
                    .orElseThrow(() -> {
                        log.warn("No unverified OTP found for userId: {}", user.getId());
                        return new OtpVerificationFailedException();
                    });

            if (!otpCode.equals(verification.getOtpCode())) {
                throw new OtpCodeInvalidException();
            }

            if (verification.isExpired()) {
                throw new OtpCodeExpiredException();
            }

            boolean isVerified = verification.verifyOtp(otpCode);
            if (!isVerified) {
                throw new OtpVerificationFailedException();
            }
            emailVerificationRepository.save(verification);

            log.info("OTP verified successfully for userId: {}", user.getId());
            return true;
        }, "OTP 검증");
    }

    private void sendEmail(String toEmail, String otpCode) {
        mailSender.sendOtpEmail(toEmail, otpCode);
    }

    private <T> T retryWithBackoff(Supplier<T> operation, String operationName) {
        int retryCount = 0;

        while (true) {
            try {
                return operation.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= MAX_RETRY) {
                    log.error("Failed to {} after {} retries due to concurrent modification", operationName, MAX_RETRY, e);
                    throw new OtpConcurrencyException(operationName + " 중 동시성 오류가 발생했습니다. 다시 시도해주세요.");
                }
                log.warn("{} 중 충돌 발생, 재시도 ({}/{})", operationName, retryCount, MAX_RETRY);
                sleepWithBackoff(retryCount);
            }
        }
    }

    private void sleepWithBackoff(int retryCount) {
        long delayMs = 100L * retryCount;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new OtpSendFailedException();
        }
    }
}