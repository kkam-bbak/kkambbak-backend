package com.kkambbak.core.entity.user;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OtpStatus status = OtpStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "verification_code")
    private String verificationCode;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * OTP 검증
     */
    public boolean verifyOtp(String inputOtp) {
        // 만료 시간 확인
        if (LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        // OTP 코드 일치 확인
        if (!otpCode.equals(inputOtp)) {
            return false;
        }

        // 검증 완료
        this.status = OtpStatus.VERIFIED;
        this.completedAt = LocalDateTime.now();
        return true;
    }

    /**
     * OTP 만료 처리
     */
    public void expireOtp() {
        this.status = OtpStatus.EXPIRED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 새로운 OTP로 재발송 준비
     */
    public void regenerateOtp(String newOtpCode, LocalDateTime newExpiresAt) {
        this.otpCode = newOtpCode;
        this.expiresAt = newExpiresAt;
        this.status = OtpStatus.PENDING;
        this.completedAt = null;
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}