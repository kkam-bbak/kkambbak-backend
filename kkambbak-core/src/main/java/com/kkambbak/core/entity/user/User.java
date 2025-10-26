package com.kkambbak.core.entity.user;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.core.entity.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
}