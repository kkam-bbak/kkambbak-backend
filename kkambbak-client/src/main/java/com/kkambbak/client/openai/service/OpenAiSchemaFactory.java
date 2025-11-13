package com.kkambbak.client.openai.service;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiSchemaFactory {
    /**
     * 롤플레이용 JSON 응답 스키마
     */
    public Map<String,Object> roleplaySchema(){
        return Map.of(
                "name", "RoleplayLine",
                "strict", false,
                "schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "korean", Map.of("type","string"),
                                "english", Map.of("type","string"),
                                "speaker", Map.of("type","string"),
                                "mismatchKorean", Map.of("type","string"),
                                "mismatchEnglish", Map.of("type","string"),
                                "coreWord", Map.of("type","string")
                        ),
                        "required", List.of("korean","english","speaker","mismatchKorean","mismatchEnglish","coreWord"),
                        "additionalProperties", false
                )
        );
    }
}
