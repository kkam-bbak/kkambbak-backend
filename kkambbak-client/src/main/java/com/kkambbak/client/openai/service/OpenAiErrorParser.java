package com.kkambbak.client.openai.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.client.openai.enums.OpenAiErrorCode;
import com.kkambbak.client.openai.exception.OpenAiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiErrorParser {

    private final ObjectMapper objectMapper;

    private static final String CODE_MODEL_NOT_FOUND = "model_not_found";
    private static final String CODE_CONTEXT_EXCEEDED = "context_length_exceeded";
    private static final String CODE_INSUFFICIENT_QUOTA = "insufficient_quota";

    public OpenAiException parse(WebClientResponseException e) {

        try {
            JsonNode root = objectMapper.readTree(e.getResponseBodyAsString());
            JsonNode error = root.path("error");

            String openAiCode = error.path("code").asText(null);
            HttpStatusCode status = e.getStatusCode();

            if (openAiCode != null) {

                if (CODE_MODEL_NOT_FOUND.equals(openAiCode)) {
                    return new OpenAiException(OpenAiErrorCode.OPEN_AI_MODEL_NOT_FOUND);
                }

                if (CODE_CONTEXT_EXCEEDED.equals(openAiCode)) {
                    return new OpenAiException(OpenAiErrorCode.OPEN_AI_CONTEXT_EXCEEDED);
                }

                if (CODE_INSUFFICIENT_QUOTA.equals(openAiCode)) {
                    return new OpenAiException(OpenAiErrorCode.OPEN_AI_QUOTA_EXCEEDED);
                }
            }

            if (status.value() == 400) {
                return new OpenAiException(OpenAiErrorCode.OPEN_AI_BAD_REQUEST_ERROR);
            }

            if (status.value() == 401) {
                return new OpenAiException(OpenAiErrorCode.OPEN_AI_UNAUTHORIZED_ERROR);
            }

            if (status.value() == 404) {
                return new OpenAiException(OpenAiErrorCode.OPEN_AI_MODEL_NOT_FOUND);
            }

            if (status.value() == 429) {
                return new OpenAiException(OpenAiErrorCode.OPEN_AI_RATE_LIMIT_ERROR);
            }

            if (status.is5xxServerError()) {
                return new OpenAiException(OpenAiErrorCode.OPEN_AI_SERVER_ERROR);
            }

            return new OpenAiException(OpenAiErrorCode.OPEN_AI_UNKNOWN_ERROR);

        } catch (Exception ex) {
            log.error("[OpenAiErrorParser] OpenAI 오류 응답 파싱 실패", ex);
            return new OpenAiException(OpenAiErrorCode.OPEN_AI_INVALID_RESPONSE_ERROR);
        }
    }
}