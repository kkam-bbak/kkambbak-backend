package com.kkambbak.domain.roleplay.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleplaySessionCompleteDto {
    private Long sessionId;
    private Integer totalSentence;
    private Integer correctSentence;
    private LocalDate completedAt;

}
