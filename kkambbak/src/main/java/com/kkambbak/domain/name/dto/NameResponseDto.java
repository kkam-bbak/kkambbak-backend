package com.kkambbak.domain.name.dto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NameResponseDto {
    private Long historyId;

    @Getter
    @Builder
    public static class GenerationOutput {
        private List<NameCandidateItemDto> names;
    }

    private GenerationOutput generationOutput;
}
