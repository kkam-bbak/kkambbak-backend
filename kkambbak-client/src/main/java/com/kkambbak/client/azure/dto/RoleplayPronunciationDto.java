package com.kkambbak.client.azure.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleplayPronunciationDto {
    private String text;
    private double accuracyScore;
    private double pronunciationScore;
    private double completenessScore;
    private double fluencyScore;
}
