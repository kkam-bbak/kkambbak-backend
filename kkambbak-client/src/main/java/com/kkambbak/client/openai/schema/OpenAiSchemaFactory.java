package com.kkambbak.client.openai.schema;


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

    /**
     * 한국어 이름 생성 JSON 응답 스키마
     */
    public Map<String, Object> koreanNameSchema() {
        return Map.of(
                "name", "KoreanNameSet",
                "strict", false,
                "schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "names", Map.of(
                                        "type", "array",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "koreanName", Map.of("type", "string"),
                                                        "romanization", Map.of("type", "string"),
                                                        "poeticMeaning", Map.of("type", "string")
                                                ),
                                                "required", List.of("koreanName", "romanization", "poeticMeaning"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 2,
                                        "maxItems", 2
                                )
                        ),
                        "required", List.of("names"),
                        "additionalProperties", false
                )
        );
    }

}
