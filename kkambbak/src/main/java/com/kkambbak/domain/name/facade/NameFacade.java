package com.kkambbak.domain.name.facade;


import com.kkambbak.client.openai.client.OpenAiClient;
import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.dto.ChatResponseDto;
import com.kkambbak.client.openai.prompts.NamePromptTemplate;
import com.kkambbak.core.entity.name.NameHistory;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.auth.exception.UserNotFoundException;
import com.kkambbak.domain.name.dto.NameCandidateItemDto;
import com.kkambbak.domain.name.dto.NameResponseDto;
import com.kkambbak.domain.name.dto.NameSelectRequestDto;
import com.kkambbak.domain.name.exception.NameGenerationExceedException;
import com.kkambbak.domain.name.service.NameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NameFacade {
    private final UserRepository userRepository;
    private final NamePromptTemplate namePromptTemplate;
    private final NameService nameService;
    private final OpenAiClient openAiClient;


    private static final String ROLE_SYSTEM = "system";
    private static final String ROLE_USER = "user";
    private static final String GPT_MODEL = "gpt-4o-mini";
    private static final String SYSTEM_MESSAGE = "You are an AI specialized in generating culturally authentic Korean names based on personality descriptions and emotional tones.";
    private static final int NAME_GENERATION_LIMIT = 3;


    public NameResponseDto generate(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("User not found");
                    return new UserNotFoundException();
        });
        int nextAttempt = nameService.calculateNextAttempt(userId);

        if (nextAttempt > NAME_GENERATION_LIMIT) {
            throw new NameGenerationExceedException();
        }

        String prompt = namePromptTemplate.buildKoreanNamePrompt(user.getGender().name(), user.getPersonalityOrImage(),user.getPreferredNameMeaning());

        List<ChatMessage> messages = List.of(
                new ChatMessage(ROLE_SYSTEM, SYSTEM_MESSAGE),
                new ChatMessage(ROLE_USER, prompt));

        ChatResponseDto response = openAiClient.getKoreanName(messages, GPT_MODEL);
        List<NameCandidateItemDto> candidates = nameService.parseCandidates(response);
        NameHistory history = nameService.generate(user, candidates, nextAttempt);

        return NameResponseDto.builder()
                .historyId(history.getId())
                .remainingAttempts(NAME_GENERATION_LIMIT-nextAttempt)
                .generationOutput(
                        NameResponseDto.GenerationOutput.builder()
                                .names(candidates)
                                .build()
                )
                .build();

    }

    public void select(Long userId, NameSelectRequestDto nameSelectRequestDto) {
        NameHistory history = nameService.validateHistoryOfUser(userId, nameSelectRequestDto.getHistoryId());
        nameService.selectName(history, nameSelectRequestDto.getKoreanName(),nameSelectRequestDto.getMeaningOfName());
    }
}
