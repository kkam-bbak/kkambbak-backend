package com.kkambbak.core.repository.user;

import com.kkambbak.core.entity.user.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /**
     * 사용자 ID로 미검증 OTP 조회 (PENDING 상태)
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.status = 'PENDING' ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestUnverifiedByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID로 OTP 코드 조회
     */
    Optional<EmailVerification> findByUserIdAndOtpCode(Long userId, String otpCode);

    /**
     * 사용자 ID로 검증 완료된 최신 OTP 조회 (VERIFIED 상태)
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.status = 'VERIFIED' ORDER BY ev.completedAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestVerifiedByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 미검증 OTP 조회 (PENDING 상태)
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.status = 'PENDING'")
    java.util.List<EmailVerification> findAllUnverifiedByUserId(@Param("userId") Long userId);

    /**
     * 검증 코드로 이메일 검증 조회
     */
    Optional<EmailVerification> findByVerificationCode(String verificationCode);
}