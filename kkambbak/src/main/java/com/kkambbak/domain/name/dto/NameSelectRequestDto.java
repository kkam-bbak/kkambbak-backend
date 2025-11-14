package com.kkambbak.domain.name.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class NameSelectRequestDto {

    @NotNull(message = "historyId는 필수 값입니다.")
    private Long historyId;

    @NotBlank(message = "koreanName은 비어 있을 수 없습니다.")
    private String koreanName;

    @NotBlank(message = "meaningOfName은 비어 있을 수 없습니다.")
    private String meaningOfName;

    public NameSelectRequestDto(Long historyId, String koreanName, String meaningOfName) {
        this.historyId = historyId;
        this.koreanName = koreanName;
        this.meaningOfName = meaningOfName;
    }
}
