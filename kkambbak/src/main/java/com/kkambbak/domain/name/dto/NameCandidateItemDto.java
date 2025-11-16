package com.kkambbak.domain.name.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NameCandidateItemDto {
    private String koreanName;
    private String romanization;
    private String poeticMeaning;
}
