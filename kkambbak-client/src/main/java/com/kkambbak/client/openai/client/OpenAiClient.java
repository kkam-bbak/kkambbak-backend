package com.kkambbak.client.openai.client;

import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.client.openai.enums.OpenAiErrorCode;
import com.kkambbak.client.openai.exception.OpenAiException;
import com.kkambbak.client.openai.service.OpenAiSchemaFactory;
import com.kkambbak.client.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClient {
    private final OpenAiSchemaFactory openAiSchemaFactory;
    private final OpenAiService openAiService;
    private static final double DEFAULT_TEMPERATURE = 0.7;


    //롤플레이
    public ChatResponseDto getRoleplaySentence(List<ChatMessage> messages, String model) {
        Map<String, Object> body = buildRequestBody(model, messages, openAiSchemaFactory.roleplaySchema());
        return openAiService.postToChat(body);
    }


    //공통
    private Map<String, Object> buildRequestBody(String model, List<ChatMessage> messages, Map<String, Object> schema) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", DEFAULT_TEMPERATURE);
        body.put("response_format", Map.of(
                "type", "json_schema",
                "json_schema", schema
        ));
        return body;
    }



}
