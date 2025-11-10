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
public class RoleplayResponseDto {
    private Long id;
    private String title;
    private String description;
    private Integer estimated_minutes;

    public static RoleplayResponseDto fromEntity(RoleplayScenario roleplayScenario) {
        return RoleplayResponseDto.builder()
                .id(roleplayScenario.getId())
                .title(roleplayScenario.getTitle())
                .description(roleplayScenario.getDescription())
                .estimated_minutes(roleplayScenario.getEstimated_minutes())
                .build();
    }

}
