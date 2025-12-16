package com.kkambbak.domain.roleplay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.client.openai.client.OpenAiClient;
import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplaySentenceDto;
import com.kkambbak.domain.roleplay.exception.GPTResponseTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoleplayGptService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private static final String GPT_MODEL = "gpt-4o-mini";


    //롤플레이 첫 시작 시
    public RoleplaySentenceDto getFirstSentence(String prompt){
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", prompt),
                new ChatMessage("user", "Let's start the roleplay.")
        );
        ChatResponseDto response = openAiClient.getRoleplaySentence(messages,GPT_MODEL);
        String content = response.getChoices().get(0).getMessage().getContent();
        return parseSentenceResponse(content);
    }

    //롤플레이 이어서 진행할 시
    public RoleplaySentenceDto continueSentence(List<ChatMessage> messages,String prevRole,String prompt){
        messages.add(new ChatMessage("user", prompt));
        ChatResponseDto response = openAiClient.getRoleplaySentence(messages,GPT_MODEL);
        String content = response.getChoices().get(0).getMessage().getContent();
        return parseSentenceResponse(content);
    }


    public RoleplaySentenceDto parseSentenceResponse(String gptContent){
        try {
            JsonNode node = objectMapper.readTree(gptContent);
            return RoleplaySentenceDto.builder()
                    .speaker(node.has("speaker") ? node.get("speaker").asText() : "")
                    .korean(node.has("korean") ? node.get("korean").asText() : "")
                    .english(node.has("english") ? node.get("english").asText() : "")
                    .mismatchKorean(node.has("mismatchKorean") ? node.get("mismatchKorean").asText() : "")
                    .mismatchEnglish(node.has("mismatchEnglish") ? node.get("mismatchEnglish").asText() : "")
                    .coreWord(node.has("coreWord")?node.get("coreWord").asText():"")
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse GPT sentence response: {}", gptContent,e);
            throw new GPTResponseTypeException();
        }
    }


}
