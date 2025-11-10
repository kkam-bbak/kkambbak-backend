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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private RoleplayDialogues roleplayDialogue;

    private String userSpeech;

    private Double score;

    @Enumerated(EnumType.STRING)
    private PronunciationResult result;

    private LocalDateTime evaluatedAt;

    public static RoleplayPronunciationFeedback create(
            RoleplayDialogues dialogue,
            User user,
            String speech,
            double score,
            PronunciationResult result
    ){
        return RoleplayPronunciationFeedback.builder()
                .roleplayDialogue(dialogue)
                .user(user)
                .userSpeech(speech)
                .score(score)
                .result(result)
                .evaluatedAt(LocalDateTime.now())
                .build();
    }


}
