package com.kkambbak.domain.roleplay.dto;

import com.kkambbak.core.entity.roleplay.RoleplayScenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleplayListResponseDto {
    private Long id;
    private String title;
    private String description;
    private Integer estimated_minutes;

    public static RoleplayListResponseDto fromEntity(RoleplayScenario roleplayScenario) {
        return RoleplayListResponseDto.builder()
                .id(roleplayScenario.getId())
                .title(roleplayScenario.getTitle())
                .description(roleplayScenario.getDescription())
                .estimated_minutes(roleplayScenario.getEstimated_minutes())
                .build();
    }

}
