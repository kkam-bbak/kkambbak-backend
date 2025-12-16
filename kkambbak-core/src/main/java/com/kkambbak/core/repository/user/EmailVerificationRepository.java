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
     * 사용자의 모든 미검증 OTP 조회 (PENDING 상태)
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.status = 'PENDING'")
    java.util.List<EmailVerification> findAllUnverifiedByUserId(@Param("userId") Long userId);

    /**
     * 검증 코드로 이메일 검증 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verificationCode = :verificationCode AND ev.status = 'PENDING' ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findByVerificationCodeAndPending(@Param("verificationCode") String verificationCode);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findMostRecentByUserId(@Param("userId") Long userId);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.status = 'PENDING' AND ev.expiresAt < CURRENT_TIMESTAMP ORDER BY ev.createdAt ASC")
    java.util.List<EmailVerification> findAllExpiredPendingOrderByCreatedAtAsc();

}