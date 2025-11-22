package com.kkambbak.domain.user.dto;

import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetProfileDto {
    private String name;
    private String koreanName;
    private String nameMeaning;
    private Gender gender;
    private String countryOfOrigin;
    private String personalityOrImage;
    private String profileImage;
    private Integer remainingNameAttempts;

    public static GetProfileDto from(User user, Integer remainingAttempts) {
        return GetProfileDto.builder()
                .name(user.getName())
                .koreanName(user.getKoreanName())
                .nameMeaning(user.getNameMeaning())
                .gender(user.getGender())
                .countryOfOrigin(user.getCountryOfOrigin())
                .personalityOrImage(user.getPersonalityOrImage())
                .profileImage(user.getProfileImage())
                .remainingNameAttempts(remainingAttempts)
                .build();
    }
}