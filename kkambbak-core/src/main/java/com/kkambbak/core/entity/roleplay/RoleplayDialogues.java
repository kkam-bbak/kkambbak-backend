package com.kkambbak.core.entity.roleplay;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "roleplay_dialogues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplayDialogues extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roleplay_session_id", nullable = false)
    private RoleplaySession roleplaySession;


    @Column(name = "turn_index", nullable = false)
    private Integer turnIndex;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpeakerType speakerType;

    @Column(nullable = false)
    private String korean;

    @Column(nullable = false)
    private String romanized;

    @Column(nullable = false)
    private String english;

    @Column(name = "mismatch_korean",nullable = false)
    private String mismatchKorean;

    @Column(name = "mismatch_english",nullable = false)
    private String mismatchEnglish;

    @Column(name = "mismatch_romanized",nullable = false)
    private String mismatchRomanized;

    @Column(nullable = false)
    private String coreWord;


    public static RoleplayDialogues create(
            RoleplaySession roleplaySession,
            String role,
            SpeakerType speakerType,
            String korean,
            String romanized,
            String english,
            String mismatchKorean,
            String mismatchEnglish,
            String mismatchRomanized,
            int turnIdx,
            String coreWord) {
        RoleplayDialogues dialogues = new RoleplayDialogues();
        dialogues.roleplaySession = roleplaySession;
        dialogues.role = role;
        dialogues.speakerType = speakerType;
        dialogues.korean = korean;
        dialogues.romanized = romanized;
        dialogues.english = english;
        dialogues.mismatchKorean = mismatchKorean;
        dialogues.mismatchEnglish = mismatchEnglish;
        dialogues.mismatchRomanized = mismatchRomanized;
        dialogues.turnIndex = turnIdx;
        dialogues.coreWord = coreWord;
        return dialogues;
    }


}
