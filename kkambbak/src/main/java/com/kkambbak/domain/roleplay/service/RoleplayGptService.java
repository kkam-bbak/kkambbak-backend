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


    //롤플레이 첫 시작 시
    public RoleplaySentenceDto getFirstSentence(String prompt){
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", prompt),
                new ChatMessage("user", "Let's start the roleplay.")
        );
        ChatResponseDto response = openAiClient.getRoleplaySentence(messages,"gpt-4o-mini",true);
        String content = response.getChoices().get(0).getMessage().getContent();
        return parseSentenceResponse(content);
    }

    //롤플레이 이어서 진행할 시
    public RoleplaySentenceDto continueSentence(List<ChatMessage> messages,String prevRole){
        String userPrompt = """
        Continue the Korean roleplay based on the previous conversation history and scenario.
        The next line should:
        - Be spoken naturally by the other person (not %s)
        - Match the tone, topic, and politeness level of the previous exchange
        - Keep both sentences concise and natural spoken Korean
        - Include a contextually mismatched but grammatically correct alternative
        
        Output exactly one JSON object with:
        {korean, english, speaker, mismatchKorean, mismatchEnglish, coreWord}
        No explanations or markdown.
        """.formatted(prevRole);
        messages.add(new ChatMessage("user", userPrompt));
        ChatResponseDto response = openAiClient.getRoleplaySentence(messages,"gpt-4o-mini",true);
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
