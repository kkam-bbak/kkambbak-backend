package com.kkambbak.domain.roleplay.dto;

import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleplayDialoguesResponseDto {
    private Long sessionId;
    private Long dialogueId;
    private String korean;
    private String romanized;
    private String english;
    private SpeakerType speaker;
    private String mismatchKorean;
    private String mismatchEnglish;
    private String mismatchRomanized;
    private String coreWord;
    private String role;
}
