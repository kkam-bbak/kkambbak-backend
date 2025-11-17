package com.kkambbak.domain.roleplay.service;

import com.kkambbak.client.azure.dto.AzurePronunciationDto;
import com.kkambbak.core.entity.roleplay.RoleplayDialogues;
import com.kkambbak.core.entity.roleplay.RoleplayPronunciationFeedback;
import com.kkambbak.core.entity.roleplay.RoleplayScenario;
import com.kkambbak.core.entity.roleplay.RoleplaySession;
import com.kkambbak.core.entity.roleplay.enums.PronunciationResult;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.repository.roleplay.RoleplayDialoguesRepository;
import com.kkambbak.core.repository.roleplay.RoleplayPronunciationFeedbackRepository;
import com.kkambbak.core.repository.roleplay.RoleplayScenariosRepository;
import com.kkambbak.core.repository.roleplay.RoleplaySessionRepository;
import com.kkambbak.core.repository.user.UserRepository;
import com.kkambbak.domain.auth.exception.UserNotFoundException;
import com.kkambbak.domain.roleplay.dto.RoleplayResponseDto;
import com.kkambbak.domain.roleplay.dto.RoleplaySentenceDto;
import com.kkambbak.domain.roleplay.exception.DialogueNotFoundException;
import com.kkambbak.domain.roleplay.exception.ScenarioNotFoundException;
import com.kkambbak.domain.roleplay.exception.SessionAndUserInconsistencyException;
import com.kkambbak.domain.roleplay.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleplayService {
    private final RoleplayScenariosRepository roleplayScenariosRepository;
    private final RoleplaySessionRepository roleplaySessionRepository;
    private final RoleplayDialoguesRepository roleplayDialoguesRepository;
    private final UserRepository userRepository;
    private final RoleplayPronunciationFeedbackRepository roleplayPronunciationFeedbackRepository;

    private static final double PRONUNCIATION_PASS_THRESHOLD = 70.0;

    @Transactional(readOnly = true)
    public List<RoleplayResponseDto> getAllRoleplayScenarios() {
        return roleplayScenariosRepository.findAll()
                .stream()
                .map(RoleplayResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleplayScenario findRoleplayScenarioById(Long roleplayScenarioId) {
        return roleplayScenariosRepository.findById(roleplayScenarioId).orElseThrow(() -> {
            log.warn("Roleplay Scenario not found");
            return new ScenarioNotFoundException();
        });
    }

    @Transactional
    public RoleplaySession saveRoleplaySession(User user,RoleplayScenario roleplayScenario) {
        RoleplaySession roleplaySession = RoleplaySession.create(user, roleplayScenario);
        roleplaySessionRepository.save(roleplaySession);
        return roleplaySession;
    }

    @Transactional
    public RoleplayDialogues saveRoleplayDialogues(RoleplaySession roleplaySession, RoleplaySentenceDto gptAnswer,
                                                   String romanized, String mismatchRomanized,int turnIdx,SpeakerType speakerType,
                                                   String coreWord) {
        RoleplayDialogues roleplayDialogue = RoleplayDialogues.create(
                roleplaySession,gptAnswer.getSpeaker(),speakerType,gptAnswer.getKorean(),
                romanized,gptAnswer.getEnglish(),gptAnswer.getMismatchKorean(),gptAnswer.getMismatchEnglish(),
                mismatchRomanized,turnIdx,coreWord);
        roleplayDialoguesRepository.save(roleplayDialogue);
        return roleplayDialogue;
    }



    @Transactional(readOnly = true)
    public RoleplaySession validateSession(Long userId, Long sessionId) {
        RoleplaySession session = roleplaySessionRepository.findById(sessionId).orElseThrow(
                ()->{
                    log.warn("Session not found");
                    return new SessionNotFoundException();
                }
        );
        if(!session.isOwner(userId)) {
            log.warn("Session and User are not same");
            throw  new SessionAndUserInconsistencyException();
        }
        return session;
    }


    @Transactional(readOnly = true)
    public RoleplayDialogues getLastDialogue(Long sessionId) {
        return roleplayDialoguesRepository.findTopByRoleplaySessionIdOrderByTurnIndexDesc(sessionId)
                .orElseThrow(()->{
                    log.warn("dialogue not found");
                    return new DialogueNotFoundException();
                });
    }

    @Transactional(readOnly = true)
    public int getNextIndex(Long sessionId) {
        Integer turn = roleplayDialoguesRepository.findMaxTurnIndexBySessionId((sessionId));
        return (turn == null) ? 1 : turn + 1;

    }

    @Transactional
    public RoleplayPronunciationFeedback saveRoleplayPronunciationFeedback(
            Long userId, RoleplayDialogues dialogues, AzurePronunciationDto pronunciationScore){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found");
                    return new UserNotFoundException();
                });
        double score = pronunciationScore.getPronunciationScore(); //PronunciationScore는 Accuracy, Completeness 등 고려한 종합 점수
        PronunciationResult pronunciationResult = score < PRONUNCIATION_PASS_THRESHOLD ? PronunciationResult.RETRY : PronunciationResult.GOOD;
        RoleplayPronunciationFeedback feedback = RoleplayPronunciationFeedback.create(
                dialogues,
                user,
                pronunciationScore.getText(),
                pronunciationScore.getAccuracyScore(),
                pronunciationScore.getPronunciationScore(),
                pronunciationScore.getCompletenessScore(),
                pronunciationScore.getFluencyScore(),
                pronunciationResult);
        roleplayPronunciationFeedbackRepository.save(feedback);
        return feedback;

    }
}
