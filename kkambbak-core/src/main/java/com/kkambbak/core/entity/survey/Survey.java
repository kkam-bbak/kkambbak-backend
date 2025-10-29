package com.kkambbak.core.entity.survey;


import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "surveys",
        uniqueConstraints = @UniqueConstraint(name = "uk_surveys_user", columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "text", nullable = false)
    private String responses;
}