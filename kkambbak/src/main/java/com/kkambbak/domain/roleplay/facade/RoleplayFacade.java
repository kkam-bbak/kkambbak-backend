package com.kkambbak.domain.roleplay.facade;


import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.core.entity.roleplay.RoleplayDialogues;
import com.kkambbak.core.entity.roleplay.RoleplayScenario;
import com.kkambbak.core.entity.roleplay.RoleplaySession;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.roleplay.RoleplayDialoguesRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.roleplay.dto.*;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import net.crizin.*;
import com.kkambbak.domain.roleplay.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleplayFacade {
    private final RoleplayService roleplayService;
    private final RoleplayGptService roleplayGptService;
    private final RoleplayCacheService roleplayCacheService;
    private final PromptTemplateService promptTemplateService;
    private final RoleplayDialoguesRepository roleplayDialoguesRepository;
    private final RoleplayCreditService roleplayCreditService;
    private final UserRepository userRepository;

    /**
     * 롤플레이 세션을 시작하고, 첫 GPT 응답을 생성한다.
     *
     * 흐름 정리:
     * 1. 잔여 크레딧 확인 및 차감
     * 2. 시나리오 조회 및 세션 생성
     * 3. GPT 첫 문장 생성 및 캐시 저장
     * 4. GPT가 응답한 문장 로마나이즈 처리
     * 5. 생성된 대화 내용을 DB에 저장하고 응답 DTO 반환
     */
    public RoleplayDialoguesResponseDto start(Long userId, Long scenarioId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found");
                    return new UserNotFoundException();
                });
        //롤플레이 시작 여부 판단 => 크레딧 초과 시 StandardCreditExceedException, PremiumCreditExceedException 반환
        roleplayCreditService.useCredit(user);

        RoleplayScenario scenario = roleplayService.findRoleplayScenarioById(scenarioId);
        RoleplaySession roleplaySession = roleplayService.saveRoleplaySession(user, scenario);

        String prompt = promptTemplateService.buildRoleplayPrompt(scenario.getTitle());
        RoleplaySentenceDto gptAnswer = roleplayGptService.getFirstSentence(prompt);
        List<ChatMessage> initMessages = List.of(
                new ChatMessage("system", prompt),
                new ChatMessage("assistant", gptAnswer.getKorean())
        );
        roleplayCacheService.saveMessages(roleplaySession.getId(),initMessages);


        String romanized = KoreanRomanizer.romanize(gptAnswer.getKorean());
        String mismatchRomanized = KoreanRomanizer.romanize(gptAnswer.getMismatchKorean());
        RoleplayDialogues roleplayDialogue = roleplayService.saveRoleplayDialogues(roleplaySession,gptAnswer,romanized,mismatchRomanized,1,SpeakerType.AI,gptAnswer.getCoreWord());

        return RoleplayDialoguesResponseDto.builder()
                .role(gptAnswer.getSpeaker())
                .sessionId(roleplaySession.getId())
                .english(roleplayDialogue.getEnglish())
                .korean(roleplayDialogue.getKorean())
                .romanized(roleplayDialogue.getRomanized())
                .dialogueId(roleplayDialogue.getId())
                .mismatchKorean(roleplayDialogue.getMismatchKorean())
                .mismatchEnglish(roleplayDialogue.getMismatchEnglish())
                .mismatchRomanized(roleplayDialogue.getMismatchRomanized())
                .speaker(roleplayDialogue.getSpeakerType())
                .coreWord(roleplayDialogue.getCoreWord())
                .build();
    }



}



