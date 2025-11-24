package com.kkambbak.domain.learning.facade;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.Vocabulary;
import com.kkambbak.core.entity.learning.enums.GradeAction;
import com.kkambbak.core.entity.learning.enums.LearningMode;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import com.kkambbak.domain.learning.dto.LearningGradeDto;
import com.kkambbak.domain.learning.dto.LearningSessionListResponse;
import com.kkambbak.domain.learning.dto.LearningStartDto;
import com.kkambbak.domain.learning.dto.SessionCardDto;
import com.kkambbak.domain.learning.exception.InvalidStartParamException;
import com.kkambbak.domain.learning.service.LearningGradeService;
import com.kkambbak.domain.learning.service.LearningListService;
import com.kkambbak.domain.learning.service.LearningStartService;
import com.kkambbak.domain.learning.support.SurveyPrefResolver.UserPref;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LearningFacade {

    private static final int FIRST_VOCABULARY_INDEX = 0;

    private final LearningStartService learningStartService;
    private final LearningGradeService learningGradeService;
    private final LearningListService learningListService;

    /** 설문을 기반으로 학습 세션 목록을 조회
     * 1. 페이징 파라미터 검증(cursor, limit)
     * 2. 사용자 설문 기반 선호도(UserPref) 조회
     * 3. 상위 노출 세션 ID 목록 조회(excludeIds) - 상위 노출에서 이미 쓰인 세션은 일반 목록에서 제외하기 위함
     * 4. 상위 노출 세션 조회(topExposure)
     * 5. 기본 세션 조회(defaultSessions)
     * 6. 상위 노출 세션과 기본 세션이 합쳐진 세션 조회
     * */
    @Transactional(readOnly = true)
    public LearningSessionListResponse getLearningList(
            Long userId,
            CategoryType category,
            Long cursor,
            int limit
    ) {
        learningListService.validatePagingParams(cursor, limit);

        Optional<UserPref> prefOpt = learningListService.resolveUserPref(userId);

        List<Long> excludeIds = learningListService.getExcludeIds(category, prefOpt);

        List<Session> topExposure = learningListService.fetchTopExposureSessions(
                category, prefOpt, cursor, limit
        );

        LearningListService.DefaultSessionResult defaultResult =
                learningListService.fetchDefaultSessions(
                        category, excludeIds, cursor, limit, topExposure.size()
                );

        List<Session> allSessions = new ArrayList<>(topExposure.size() + defaultResult.sessions().size());
        allSessions.addAll(topExposure);
        allSessions.addAll(defaultResult.sessions());

        if (allSessions.isEmpty()) {
            return LearningSessionListResponse.of(category, List.of(), null, false);
        }

        List<Long> sessionIds = allSessions.stream()
                .map(Session::getId)
                .toList();

        Map<Long, Integer> vocabCountMap = learningListService.fetchVocabularyCounts(sessionIds);
        Map<Long, LearningResult> resultMap = learningListService.fetchLearningResults(userId, sessionIds);

        List<SessionCardDto> dtos = convertToSessionCards(allSessions, vocabCountMap, resultMap);

        PagingInfo pagingInfo = calculatePagingInfo(
                topExposure, defaultResult.sessions(), defaultResult.hasMore()
        );

        return LearningSessionListResponse.of(
                category,
                dtos,
                pagingInfo.nextCursor(),
                pagingInfo.hasNext()
        );
    }

    /**
     * 학습 세션을 시작하고, 학습에 필요한 첫 단어/학습 결과(LearningResult)를 준비한다.
     * 1. 입력 검증
     * 2. 세션 정보 로딩
     * 3. 학습 모드 결정 (ALL / WRONG_ONLY)
     *    - ALL      → 세션 전체 단어 ID 목록 조회
     *    - WRONG_ONLY → baseResultId 기준으로 '틀린 단어' ID 목록만 필터링
     * 4. 이번 학습에서 사용할 vocabularyId 목록 생성
     *    - 첫 번째 문제를 포함한 전체 학습 순서를 결정하는 핵심 단계
     * 5. 새로운 LearningResult 생성
     * 6. 첫 단어(FirstVocabulary) DTO 생성
     * 7. 응답 DTO 조립 및 반환
     */

    @Transactional
    public LearningStartDto.StartResponse startLearning(
            Long userId,
            Long sessionId,
            LearningStartDto.StartRequest req
    ) {
        learningStartService.validateInputs(userId, sessionId, req);


        Session session = learningStartService.loadSession(sessionId);

        LearningMode mode = Optional.ofNullable(req.getMode())
                .orElse(LearningMode.ALL);

        final List<Long> vocabIds;
        Long restartFromResultId = null;

        switch (mode) {
            case ALL -> vocabIds = learningStartService.getAllVocabularyIds(sessionId);
            case WRONG_ONLY -> {
                vocabIds = learningStartService.getWrongVocabularyIds(userId, sessionId, req);
                restartFromResultId = req.getBaseResultId();
            }
            default -> throw new InvalidStartParamException("지원하지 않는 학습 모드입니다: " + mode);
        }

        LearningResult result = learningStartService.createNewResult(userId, session, vocabIds.size());

        LearningStartDto.StartResponse.FirstVocabulary firstVocabulary =
                learningStartService.makeFirstVocabulary(vocabIds.get(FIRST_VOCABULARY_INDEX));

        return LearningStartDto.StartResponse.of(
                session.getId(),
                result.getId(),
                vocabIds,
                firstVocabulary,
                restartFromResultId,
                session

        );
    }


    /**
     * 단어 단위 채점 흐름을 처리한다.
     * - 정답/오답 판정, 결과 DB 반영, 다음 문제/정답 공개/학습 종료 판단까지 모두 담당.
     * 1. 기본 요청 검증
     * 2. 학습 컨텍스트 로딩
     *    - Session 로딩
     *    - 해당 user의 최신 LearningResult 로딩
     *    - vocabIds(이번 세션의 학습 순서) 로딩
     *    - 현재 단어 Vocabulary 로딩
     *    - 현재 단어의 순서(orderIndex) 계산
     * 3. 채점
     *    - action == GRADE → Azure STT로 발음 평가 후 정답판정
     *    - action == NEXT_AFTER_WRONG -> 오답 확정 후 다음 단어
     */
    @Transactional
    public LearningGradeDto.GradeResponse gradeLearning(
            Long userId,
            Long sessionId,
            LearningGradeDto.GradeRequest req,
            MultipartFile audioFile
    ) {
        learningGradeService.validateBasicInputs(req, audioFile);

        Session session = learningGradeService.loadSession(sessionId);
        LearningResult result = learningGradeService.findLatestResult(userId, sessionId);
        List<Long> vocabIds = learningGradeService.loadVocabOrder(sessionId);
        Vocabulary currentVocab = learningGradeService.loadVocabulary(req.getItemId());
        int orderIndex = learningGradeService.findOrderIndex(vocabIds, req.getItemId());

        GradeAction action = req.getAction();

        var eval = learningGradeService.evaluateCorrectness(action, audioFile, currentVocab);
        boolean isCorrect = eval.correct();
        Double score = eval.score();

        learningGradeService.applyGradingResult(result, currentVocab, action, isCorrect);

        boolean isLast = isLastItem(orderIndex, vocabIds.size());
        FlowDecision flow = decideFlow(action, isCorrect, isLast, orderIndex, vocabIds, currentVocab);

        if (flow.finished()) {
            result.complete();
        }

        return LearningGradeDto.GradeResponse.of(
                isCorrect,
                flow.moved(),
                flow.finished(),
                flow.next(),
                flow.correctAnswer(),
                score
        );
    }


    // Facade 내부 헬퍼들

    private boolean isLastItem(int orderIndex, int totalSize) {
        return orderIndex == totalSize - 1;
    }

    /**
     * action(GRADE / NEXT_AFTER_WRONG)에 따라
     * 다음 흐름을 결정하는 상위 분기.
     */
    private FlowDecision decideFlow(
            GradeAction action,
            boolean isCorrect,
            boolean isLast,
            int orderIndex,
            List<Long> vocabIds,
            Vocabulary currentVocab
    ) {
        return switch (action) {
            case GRADE ->
                    decideFlowForGrade(isCorrect, isLast, orderIndex, vocabIds);
            case NEXT_AFTER_WRONG ->
                    decideFlowForNextAfterWrong(isLast, orderIndex, vocabIds, currentVocab);
        };
    }

    // GRADE 일 때
    private FlowDecision decideFlowForGrade(
            boolean isCorrect,
            boolean isLast,
            int orderIndex,
            List<Long> vocabIds
    ) {
        // 오답 → 현재 단어 재학습
        if (!isCorrect) {
            return new FlowDecision(false, false, null, null);
        }
        // 정답 + 마지막 단어 → 종료
        if (isLast) {
            return new FlowDecision(true, true, null, null);
        }
        // 정답 + 다음 단어 존재 → 다음 단어로 이동
        int nextIndex = orderIndex + 1;
        LearningGradeDto.Next next = buildNextDto(nextIndex, vocabIds);

        return new FlowDecision(true, false, next, null);
    }

    // NEXT_AFTER_WRONG 일때
    private FlowDecision decideFlowForNextAfterWrong(
            boolean isLast,
            int orderIndex,
            List<Long> vocabIds,
            Vocabulary currentVocab
    ) {
        // 정답 공개 DTO
        LearningGradeDto.CorrectAnswer correctAnswer = buildCorrectAnswerDto(currentVocab);
        // 마지막 단어면 종료
        if (isLast) {
            return new FlowDecision(true, true, null, correctAnswer);
        }
        // 다음 단어로 이동
        int nextIndex = orderIndex + 1;
        LearningGradeDto.Next next = buildNextDto(nextIndex, vocabIds);

        return new FlowDecision(true, false, next, correctAnswer);
    }

    private LearningGradeDto.Next buildNextDto(int nextIndex, List<Long> vocabIds) {
        Long nextVocabId = vocabIds.get(nextIndex);
        Vocabulary nextVocab = learningGradeService.loadVocabulary(nextVocabId);

        return LearningGradeDto.Next.builder()
                .itemId(nextVocabId)
                .korean(nextVocab.getKorean())
                .english(nextVocab.getEnglish())
                .romanization(nextVocab.getRomanized())
                .imageUrl(nextVocab.getImageUrl())
                .build();
    }

    private LearningGradeDto.CorrectAnswer buildCorrectAnswerDto(Vocabulary vocab) {
        return LearningGradeDto.CorrectAnswer.builder()
                .itemId(vocab.getId())
                .korean(vocab.getKorean())
                .romanization(vocab.getRomanized())
                .english(vocab.getEnglish())
                .imageUrl(vocab.getImageUrl())
                .build();
    }

    private List<SessionCardDto> convertToSessionCards(
            List<Session> sessions,
            Map<Long, Integer> vocabCountMap,
            Map<Long, LearningResult> resultMap
    ) {
        return sessions.stream()
                .map(session -> {
                    int vocabCount = vocabCountMap.getOrDefault(session.getId(), 0);
                    LearningResult result = resultMap.get(session.getId());
                    return SessionCardDto.of(session, result, vocabCount);
                })
                .toList();
    }

    // nextCursor / hasNext 계산
    private PagingInfo calculatePagingInfo(
            List<Session> topExposure,
            List<Session> defaults,
            boolean hasMoreDefaults
    ) {
        Long nextCursor;
        boolean hasNext;

        if (!defaults.isEmpty()) {
            nextCursor = defaults.get(defaults.size() - 1).getId();
            hasNext = hasMoreDefaults;
        } else if (!topExposure.isEmpty()) {
            nextCursor = topExposure.get(topExposure.size() - 1).getId();
            hasNext = hasMoreDefaults;
        } else {
            nextCursor = null;
            hasNext = false;
        }

        return new PagingInfo(nextCursor, hasNext);
    }

    // Facade 내부에서만 쓰는 레코드들
    private record FlowDecision(
            boolean moved, // 다음 문제로 넘어갈 수 있는지 여부
            boolean finished, // 학습이 끝났는지
            LearningGradeDto.Next next,
            LearningGradeDto.CorrectAnswer correctAnswer
    ) {}

    private record PagingInfo(Long nextCursor, boolean hasNext) {}
}