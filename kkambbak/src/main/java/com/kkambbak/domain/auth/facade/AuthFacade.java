package com.kkambbak.domain.auth.facade;

import com.kkambbak.core.entity.user.EmailVerification;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.OtpStatus;
import com.kkambbak.core.entity.user.enums.UserStatus;
import com.kkambbak.core.repository.user.EmailVerificationRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.auth.exception.OtpVerificationFailedException;
import com.kkambbak.domain.auth.exception.VerificationCodeNotFoundException;
import com.kkambbak.domain.auth.exception.VerificationCodeAlreadyUsedException;
import com.kkambbak.domain.auth.exception.UserAlreadyAuthenticatedException;
import com.kkambbak.domain.auth.exception.UserNotFoundException;
import com.kkambbak.domain.auth.service.EmailService;
import com.kkambbak.global.jwt.JwtUtil;
import com.kkambbak.global.jwt.dto.TokenDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public TokenDataDto verifyOtp(String email, String otpCode) {
        if (!emailService.verifyOtp(email, otpCode)) {
            throw new OtpVerificationFailedException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user = user.toBuilder()
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        return jwtUtil.createTokenData(user.getId());
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UserAlreadyAuthenticatedException();
        }

        emailService.sendOtpEmail(email, false);
    }

    public String getEmailByCode(String code) {
        EmailVerification emailVerification = emailVerificationRepository.findByVerificationCodeAndPending(code)
                .orElseThrow(VerificationCodeNotFoundException::new);

        if (OtpStatus.EXPIRED.equals(emailVerification.getStatus())) {
            throw new VerificationCodeAlreadyUsedException();
        }

        if (OtpStatus.VERIFIED.equals(emailVerification.getStatus())) {
            throw new VerificationCodeAlreadyUsedException();
        }

        User user = userRepository.findById(emailVerification.getUserId())
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - userId: {}", emailVerification.getUserId());
                    return new UserNotFoundException();
                });

        return user.getEmail();
    }
}