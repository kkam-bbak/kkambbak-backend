package com.kkambbak.client.openai.client;

import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.client.openai.schema.OpenAiSchemaFactory;
import com.kkambbak.client.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClient {
    private final OpenAiSchemaFactory openAiSchemaFactory;
    private final OpenAiService openAiService;
    private static final double DEFAULT_TEMPERATURE = 0.7;

    private static final String KEY_MODEL = "model";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_RESPONSE_FORMAT = "response_format";
    private static final String KEY_TYPE = "type";
    private static final String KEY_JSON_SCHEMA = "json_schema";

    private static final String RESPONSE_TYPE_JSON_SCHEMA = "json_schema";


    //롤플레이
    public ChatResponseDto getRoleplaySentence(List<ChatMessage> messages, String model) {
        Map<String, Object> body = buildRequestBody(model, messages, openAiSchemaFactory.roleplaySchema());
        return openAiService.postToChat(body);
    }


    //공통
    private Map<String, Object> buildRequestBody(String model, List<ChatMessage> messages, Map<String, Object> schema) {
        Map<String, Object> body = new HashMap<>();
        body.put(KEY_MODEL, model);
        body.put(KEY_MESSAGES, messages);
        body.put(KEY_TEMPERATURE, DEFAULT_TEMPERATURE);
        body.put(KEY_RESPONSE_FORMAT, Map.of(
                KEY_TYPE, RESPONSE_TYPE_JSON_SCHEMA,
                KEY_JSON_SCHEMA, schema
        ));
        return body;
    }



}
