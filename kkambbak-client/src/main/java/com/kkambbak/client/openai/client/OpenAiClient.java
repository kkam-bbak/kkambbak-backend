package com.kkambbak.client.openai.client;

import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiClient {


    private final WebClient openAiWebClient;

    private static final double DEFAULT_TEMPERATURE = 1.0;

    public OpenAiClient(@Qualifier("openAiWebClient") WebClient openAiWebClient) {
        this.openAiWebClient = openAiWebClient;
    }

    public ChatResponseDto getRoleplaySentence(List<ChatMessage> messages, String model, boolean jsonForce) {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", DEFAULT_TEMPERATURE);

        if (jsonForce) {
            Map<String, Object> schema = Map.of(
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
            requestBody.put("response_format", Map.of(
                    "type", "json_schema",
                    "json_schema", schema
            ));
        }

        ChatResponseDto response = openAiWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatResponseDto.class)
                .doOnNext(res -> {
                    if(res.getChoices()!=null && !res.getChoices().isEmpty()){
                        log.info("[OpenAI 응답 완료] message={}",
                                res.getChoices().get(0).getMessage().getContent());
                    }
                })
                .block();

        return response;
    }

}
