package com.kkambbak.client.openai.service;

import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.client.openai.enums.OpenAiErrorCode;
import com.kkambbak.client.openai.exception.OpenAiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {
    private final WebClient openAiWebClient;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 모든 OpenAI API의 공통 POST 호출
     */
    public ChatResponseDto postToChat(Object body){
        try{
            return openAiWebClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            Mono.error(new OpenAiException(OpenAiErrorCode.OPEN_AI_SERVER_ERROR))
                    )
                    .bodyToMono(ChatResponseDto.class)
                    .doOnNext(res -> {
                        if(res.getChoices()!=null && !res.getChoices().isEmpty()){
                            log.info("[OpenAI 응답 완료] message={}",
                                    res.getChoices().get(0).getMessage().getContent());
                        }
                    })
                    .timeout(DEFAULT_TIMEOUT)
                    .block();
        }catch (Exception e){
            if (e.getCause() instanceof TimeoutException) {
                log.error("[OpenAI Timeout] 요청이 30초를 초과했습니다.");
                throw new OpenAiException(OpenAiErrorCode.OPEN_AI_TIMEOUT);
            }
            if (e instanceof WebClientResponseException webEx) {
                log.error("[OpenAI API Error] status={}, body={}",
                        webEx.getStatusCode(), webEx.getResponseBodyAsString());
                throw new OpenAiException(OpenAiErrorCode.OPEN_AI_INVALID_RESPONSE_ERROR);
            }
            log.error("[OpenAI Unexpected Error] type={}, message={}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new OpenAiException(OpenAiErrorCode.OPEN_AI_UNKNOWN_ERROR);
        }
    }
}
