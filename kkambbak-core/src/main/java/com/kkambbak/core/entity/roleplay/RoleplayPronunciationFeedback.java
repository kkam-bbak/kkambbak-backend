package com.kkambbak.core.entity.roleplay;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.roleplay.enums.PronunciationResult;
import com.kkambbak.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "roleplay_pronunciation_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplayPronunciationFeedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleplay_dialogue_id")
    private RoleplayDialogues roleplayDialogue;

    @Column(nullable = false)
    private String userSpeech;

    @Column(nullable = false)
    private double accuracyScore;

    @Column(nullable = false)
    private double pronunciationScore;

    @Column(nullable = false)
    private double completenessScore;

    @Column(nullable = false)
    private double fluencyScore;

    @Enumerated(EnumType.STRING)
    private PronunciationResult result;

    private LocalDateTime evaluatedAt;


    public static RoleplayPronunciationFeedback create(
            RoleplayDialogues dialogue,
            User user,
            String speech,
            double accuracyScore,
            double pronunciationScore,
            double completenessScore,
            double fluencyScore,
            PronunciationResult result
    ){
        return RoleplayPronunciationFeedback.builder()
                .roleplayDialogue(dialogue)
                .user(user)
                .userSpeech(speech)
                .accuracyScore(accuracyScore)
                .pronunciationScore(pronunciationScore)
                .completenessScore(completenessScore)
                .fluencyScore(fluencyScore)
                .result(result)
                .evaluatedAt(LocalDateTime.now())
                .build();
    }
}
