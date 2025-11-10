package com.kkambbak.domain.roleplay.dto;

import com.kkambbak.core.entity.roleplay.enums.PronunciationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleplayEvaluateResponseDto {
    private Long dialogueId;
    private double score;
    private PronunciationResult feedback;
}
