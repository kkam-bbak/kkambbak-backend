package com.kkambbak.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class registerKoreanDto {
    private String preferredNameMeaning;
    private String personalityOrImage;
}