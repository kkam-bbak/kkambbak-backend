package com.kkambbak.domain.roleplay.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleplaySentenceDto {
    private String korean;
    private String english;
    private String speaker;
    private String mismatchKorean;
    private String mismatchEnglish;
    private String coreWord;
}
