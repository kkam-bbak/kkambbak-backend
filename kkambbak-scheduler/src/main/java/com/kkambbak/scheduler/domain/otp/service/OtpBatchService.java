package com.kkambbak.scheduler.domain.otp.service;

import com.kkambbak.core.entity.user.EmailVerification;
import com.kkambbak.core.repository.user.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OtpBatchService {

    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional(readOnly = true)
    public List<EmailVerification> getExpireTargets() {
        List<EmailVerification> expiredPendingOtps =
            emailVerificationRepository.findAllExpiredPendingOrderByCreatedAtAsc();

        log.info("Found {} expired pending OTPs", expiredPendingOtps.size());
        return expiredPendingOtps;
    }

    @Transactional
    public void expireAllOtps(List<EmailVerification> emailVerifications) {
        if (emailVerifications.isEmpty()) {
            return;
        }

        for (EmailVerification emailVerification : emailVerifications) {
            emailVerification.expireOtp();
        }

        emailVerificationRepository.saveAll(emailVerifications);
    }

    @Transactional
    public void expireOtp(EmailVerification emailVerification) {
        emailVerification.expireOtp();
        emailVerificationRepository.save(emailVerification);
    }
}