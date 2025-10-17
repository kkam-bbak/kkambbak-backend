package com.kkambbak.core.entity.user;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.entity.user.enums.Gender;
import com.kkambbak.core.entity.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthdate;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Column(name = "korean_name", length = 100)
    private String koreanName;

    @Column(name = "name_meaning", columnDefinition = "TEXT")
    private String nameMeaning;

    @Column(name = "profile_card", columnDefinition = "TEXT")
    private String profileCard;

    @Builder.Default
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Builder.Default
    @Column(name = "is_guest")
    private Boolean isGuest = false;

    @Column(name = "guest_id", length = 50)
    private String guestId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.GOOGLE;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public User updateFromOAuth2(String email, String firstName, String lastName, String profileImage) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
        return this;
    }
}