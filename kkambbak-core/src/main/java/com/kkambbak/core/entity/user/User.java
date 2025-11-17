package com.kkambbak.core.entity.user;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.core.entity.user.enums.UserStatus;
import com.kkambbak.core.entity.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String email;

    @Column(name = "name", length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Column(name = "korean_name", length = 100)
    private String koreanName;

    @Column(name = "name_meaning", columnDefinition = "TEXT")
    private String nameMeaning;

    @Column(name = "personality_or_image", columnDefinition = "TEXT")
    private String personalityOrImage;

    @Column(name = "preferred_name_meaning", columnDefinition = "TEXT")
    private String preferredNameMeaning;

    @Column(name = "profile_card", columnDefinition = "TEXT")
    private String profileCard;

    @Builder.Default
    @Column(name = "is_guest")
    private Boolean isGuest = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.GOOGLE;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.STANDARD;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @Builder.Default
    @Column(name = "roleplay_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer roleplayCount =0;

    public User updateFromOAuth2(String email, String name, String profileImage) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        return this;
    }

    // 게스트 사용자를 Google 계정으로 업그레이드
    public void upgradeToGoogleUser(AuthProvider provider, String providerId,
                                    String email, String name, String profileImage) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.isGuest = false;
        this.status = UserStatus.PENDING;
    }

    public void updateProfile(String name, Gender gender, String countryOfOrigin, String profileImage) {
        this.name = name;
        this.gender = gender;
        this.countryOfOrigin = countryOfOrigin;
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    //롤플레이 월 사용 횟수 증가
    public void incrementUsage() {
        this.roleplayCount += 1;
    }

    //롤플레이 월 사용 횟수 초기화 (추후 스케줄러)
    public void resetUsage() {
        this.roleplayCount = 0;
    }


    //한국어 이름 생성
    public void updateKoreanName(String koreanName,String nameMeaning) {
        this.koreanName = koreanName;
        this.nameMeaning = nameMeaning;
    }

    public void updateKoreanNameWithPersonality(String preferredNameMeaning, String personalityOrImage) {
        this.preferredNameMeaning = preferredNameMeaning;
        this.personalityOrImage = personalityOrImage;
    }
}