package com.kkambbak.domain.roleplay.facade;


import com.kkambbak.client.azure.dto.AzurePronunciationDto;
import com.kkambbak.client.azure.service.AzurePronunciationService;
import com.kkambbak.client.openai.dto.ChatMessage;
import com.kkambbak.client.openai.prompts.RoleplayPromptTemplate;
import com.kkambbak.core.entity.roleplay.RoleplayDialogues;
import com.kkambbak.core.entity.roleplay.RoleplayPronunciationFeedback;
import com.kkambbak.core.entity.roleplay.RoleplayScenario;
import com.kkambbak.core.entity.roleplay.RoleplaySession;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.roleplay.RoleplayDialoguesRepository;
import com.kkambbak.core.repository.roleplay.RoleplayPronunciationFeedbackRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.roleplay.dto.*;
import com.kkambbak.domain.roleplay.exception.DialogueLimitExceedException;
import com.kkambbak.domain.roleplay.exception.DialogueNotFoundException;
import com.kkambbak.domain.roleplay.exception.NoContentInSessionException;
import com.kkambbak.domain.roleplay.exception.PronunciationLimitExceedException;
import com.kkambbak.domain.user.exception.UserNotFoundException;
import net.crizin.*;
import com.kkambbak.domain.roleplay.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleplayFacade {
    private final RoleplayService roleplayService;
    private final RoleplayGptService roleplayGptService;
    private final RoleplayCacheService roleplayCacheService;
    private final RoleplayDialoguesRepository roleplayDialoguesRepository;
    private final RoleplayCreditService roleplayCreditService;
    private final RoleplayPromptTemplate roleplayPromptTemplate;
    private final UserRepository userRepository;
    private final RoleplayPronunciationFeedbackRepository roleplayPronunciationFeedbackRepository;
    private final AudioConvertService audioConvertService;
    private final AzurePronunciationService roleplayPronunciationService;

    private static final int MAX_TURN_PER_SPEAKER = 3;
    private static final int FIRST_TURN_INDEX = 1;
    private static final String ROLE_SYSTEM = "system";
    private static final String ROLE_ASSISTANT = "assistant";

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

        String prompt = roleplayPromptTemplate.buildRoleplayStartPrompt(roleplaySession.getScenario().getTitle());

        RoleplaySentenceDto gptAnswer = roleplayGptService.getFirstSentence(prompt);
        List<ChatMessage> initMessages = List.of(
                new ChatMessage(ROLE_SYSTEM, prompt),
                new ChatMessage(ROLE_ASSISTANT, gptAnswer.getKorean())
        );
        roleplayCacheService.saveMessages(roleplaySession.getId(),initMessages);


        String romanized = KoreanRomanizer.romanize(gptAnswer.getKorean());
        String mismatchRomanized = KoreanRomanizer.romanize(gptAnswer.getMismatchKorean());
        RoleplayDialogues roleplayDialogue = roleplayService.saveRoleplayDialogues(roleplaySession,gptAnswer,romanized,mismatchRomanized,FIRST_TURN_INDEX,SpeakerType.AI,gptAnswer.getCoreWord());

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

    /**
     * 롤플레이 세션을 이어서 진행하며 GPT 응답을 생성한다.
     *
     * 흐름 정리:
     * 1. 세션 유효성 검증 및 캐시된 대화 이력 조회
     * 2. 직전 화자 정보를 기반으로 GPT 응답 생성
     * 3. 캐시 메시지 갱신 및 저장
     * 4. GPT가 응답한 문장 로마나이즈 처리
     * 5. 생성된 대화 내용을 DB에 저장하고 응답 DTO 반환
     */
    public RoleplayDialoguesResponseDto next(Long userId,Long sessionId) {
        RoleplaySession roleplaySession = roleplayService.validateSession(userId,sessionId);
        int gptTurn = roleplayDialoguesRepository.countByRoleplaySession_IdAndSpeakerType(roleplaySession.getId(), SpeakerType.AI);
        int userTurn = roleplayDialoguesRepository.countByRoleplaySession_IdAndSpeakerType(roleplaySession.getId(), SpeakerType.USER);

        if(gptTurn>=MAX_TURN_PER_SPEAKER&&userTurn>=MAX_TURN_PER_SPEAKER){
            log.warn("Dialogue limit has been reached");
            throw new DialogueLimitExceedException();
        }

        List<ChatMessage> messages = roleplayCacheService.getMessages(sessionId);
        if(messages.isEmpty()){
            throw new NoContentInSessionException();
        }
        RoleplayDialogues lastDialogue = roleplayService.getLastDialogue(sessionId);

        String prevRole = lastDialogue.getRole();
        String prompt = roleplayPromptTemplate.buildRoleplayNextPrompt(prevRole);
        RoleplaySentenceDto gptAnswer = roleplayGptService.continueSentence(messages, prevRole,prompt);
        messages.add(new ChatMessage(ROLE_ASSISTANT, gptAnswer.getKorean()));
        roleplayCacheService.saveMessages(sessionId,messages);


        String romanized = KoreanRomanizer.romanize(gptAnswer.getKorean());
        String mismatchRomanized = KoreanRomanizer.romanize(gptAnswer.getMismatchKorean());

        int nextIndex = roleplayService.getNextIndex(roleplaySession.getId());
        SpeakerType nextSpeakerType = (lastDialogue.getSpeakerType()==SpeakerType.AI) ? SpeakerType.USER:SpeakerType.AI ;
        RoleplayDialogues roleplayDialogue = roleplayService.saveRoleplayDialogues(roleplaySession, gptAnswer, romanized,mismatchRomanized,nextIndex, nextSpeakerType,gptAnswer.getCoreWord());

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

    /**
     * 롤플레이 대화의 발음 평가를 수행하고 결과를 반환한다.
     *
     * 흐름 정리:
     * 1. 세션 유효성 검증 및 대상 대화(발음할 문장) 조회
     * 2. 발음 평가 가능 여부 판단(제한(2번) 초과했는지 여부)
     * 3. 업로드된 오디오 파일을 WAV 포맷으로 변환
     * 4. 발음 평가 서비스 호출 (Azure)
     * 5. 평가 결과를 DB에 저장
     * 6. 피드백 및 점수를 포함한 응답 DTO 반환
     */
    public RoleplayEvaluateResponseDto evaluate(Long userId, Long sessionId, Long dialogueId, MultipartFile audioFile) throws IOException, ExecutionException, InterruptedException, UnsupportedAudioFileException {
        RoleplaySession roleplaySession = roleplayService.validateSession(userId,sessionId);
        RoleplayDialogues dialogues = roleplayDialoguesRepository.findById(dialogueId).orElseThrow(
                DialogueNotFoundException::new);

        //평가 가능 여부 판단(한 번만 가능)
        boolean alreadyEvaluated = roleplayPronunciationFeedbackRepository.existsByRoleplayDialogue_Id(dialogueId);
        if(alreadyEvaluated){
            log.warn("Pronunciation feedback has been reached");
            throw new PronunciationLimitExceedException();
        }
        File wavFile = audioConvertService.toWav(audioFile);
        AzurePronunciationDto pronunciationScore = roleplayPronunciationService.getPronunciationScore(dialogues.getKorean(), wavFile);
        RoleplayPronunciationFeedback feedback = roleplayService.saveRoleplayPronunciationFeedback(userId, dialogues, pronunciationScore);
        return RoleplayEvaluateResponseDto.builder()
                .dialogueId(dialogues.getId())
                .feedback(feedback.getResult())
                .score(feedback.getPronunciationScore())
                .build();
    }


    @Transactional
    public RoleplaySessionCompleteDto complete(Long userId, Long sessionId) {
        RoleplaySession roleplaySession = roleplayService.validateSession(userId,sessionId);
        int attemptCount = roleplayPronunciationFeedbackRepository.countAttemptedDialogues(roleplaySession.getId());
        int goodCount = roleplayPronunciationFeedbackRepository.countGoodDialogues(roleplaySession.getId());

        roleplaySession.endState();
        roleplayCacheService.clear(roleplaySession.getId());

        LocalDateTime completed = roleplaySession.getCompletedAt();
        return RoleplaySessionCompleteDto.builder()
                .sessionId(sessionId)
                .totalSentence(attemptCount)
                .correctSentence(goodCount)
                .completedAt(completed.toLocalDate())
                .build();
    }




}



