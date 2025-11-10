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
    private RoleplaySession roleplaySession;


    @Column(name = "turn_index")
    private Integer turnIndex;

    private String role;

    @Enumerated(EnumType.STRING)
    private SpeakerType speakerType;

    private String korean;

    private String romanized;

    private String english;

    @Column(name = "mismatch_korean")
    private String mismatchKorean;

    @Column(name = "mismatch_english")
    private String mismatchEnglish;

    @Column(name = "mismatch_romanized")
    private String mismatchRomanized;

    private String coreWord;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

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
            String audioUrl,
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
        dialogues.audioUrl = audioUrl;
        dialogues.coreWord = coreWord;
        return dialogues;
    }


}
