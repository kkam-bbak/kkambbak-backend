package com.kkambbak.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDto {
    private String name;
    private String gender;
    private String countryOfOrigin;
    private String profileImage;
}