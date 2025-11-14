package com.kkambbak.domain.name.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameSelectRequestDto {
    private Long historyId;
    private String koreanName;
    private String meaningOfName;
}
