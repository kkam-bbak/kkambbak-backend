package com.kkambbak.scheduler.domain.otp.job;

import com.kkambbak.core.entity.user.EmailVerification;
import com.kkambbak.scheduler.domain.otp.service.OtpBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpExpireJob {

    private final OtpBatchService otpBatchService;

    @Scheduled(cron = "${cron.otp-expire-job}")
    public void executeJob() {
        List<EmailVerification> otpsToExpire = otpBatchService.getExpireTargets();

        try {
            otpBatchService.expireAllOtps(otpsToExpire);

        } catch (Exception e) {
            log.warn("[OtpExpireJob] Batch expiration failed, retrying individually. Error: {}", e.getMessage());

            for (EmailVerification emailVerification : otpsToExpire) {
                try {
                    otpBatchService.expireOtp(emailVerification);
                } catch (Exception retryException) {
                    log.error("[OtpExpireJob] Failed to expire OTP (retry) - ID: {}, Error: {}",
                        emailVerification.getId(), retryException.getMessage());
                }
            }
        }
    }
}