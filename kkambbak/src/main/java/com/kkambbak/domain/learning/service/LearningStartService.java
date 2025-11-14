package com.kkambbak.domain.learning.service;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.Vocabulary;
import com.kkambbak.core.entity.learning.enums.LearningMode;
import com.kkambbak.core.repository.learning.*;
import com.kkambbak.domain.learning.dto.LearningStartDto;
import com.kkambbak.domain.learning.exception.InvalidStartParamException;
import com.kkambbak.domain.learning.exception.LearningDataInconsistencyException;
import com.kkambbak.domain.learning.exception.LearningResultNotFoundException;
import com.kkambbak.domain.learning.exception.NoWrongVocabularyException;
import com.kkambbak.domain.learning.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningStartService {

    private static final int FIRST_VOCABULARY_INDEX = 0;

    private final SessionRepository sessionRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final LearningResultRepository learningResultRepository;
    private final LearningResultDetailsRepository learningResultDetailsRepository;

    @Transactional
    public LearningStartDto.StartResponse start(Long userId,
                                                Long sessionId,
                                                LearningStartDto.StartRequest req) {

        // 입력값 검증
        validateInputs(userId, sessionId, req);

        // 세션이 존재하는지 조회
        Session session = loadSession(sessionId);

        // 요청에서 학습 모드 가져오기 (all 전체학습, wrong_only 틀린것만 학습)
        LearningMode mode = req.getMode();
        if (mode == null) {
            mode = LearningMode.ALL;
        }

        final List<Long> vocabIds;
        // 틀린 것만 학습 시 기준이 되는 이전 결과 ID
        Long restartFromResultId = null;

        // 단어 목록 가져오기
        switch (mode) {
            case ALL -> vocabIds = getAllVocabularyIds(sessionId);
            case WRONG_ONLY -> {
                vocabIds = getWrongVocabularyIds(userId, sessionId, req);
                restartFromResultId = req.getBaseResultId();
            }
            default -> throw new InvalidStartParamException("지원하지 않는 학습 모드입니다: " + mode);
        }

        // 기존 학습 결과 삭제
        removeAllResultsFor(userId, sessionId);

        // 새 학습 결과 생성 및 저장
        LearningResult saved = learningResultRepository.save(
                LearningResult.startOf(userId, session, vocabIds.size())
        );

        // 첫 단어 정보 만들기
        var firstVocabulary = makeFirstVocabulary(vocabIds.get(FIRST_VOCABULARY_INDEX));

        return LearningStartDto.StartResponse.of(
                session.getId(),
                saved.getId(),
                vocabIds,
                firstVocabulary,
                restartFromResultId
        );
    }




    // 입력값 검증
    private void validateInputs(Long userId, Long sessionId, LearningStartDto.StartRequest req) {
        if (userId == null || sessionId == null) {
            throw new InvalidStartParamException("userId 또는 sessionId가 null입니다.");
        }
    }


    // 세션이 존재하는지 조회
    private Session loadSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "세션을 찾을 수 없습니다. sessionId=" + sessionId
                ));
    }


    // 전체 학습일 때 단어 목록 가져오기
    private List<Long> getAllVocabularyIds(Long sessionId) {
        List<Long> vocabIds = sessionVocabularyRepository
                .findVocabularyIdsOrderByVocabIdAsc(sessionId);

        if (vocabIds.isEmpty()) {
            throw new LearningDataInconsistencyException(
                    "세션에 연결된 단어가 없습니다. sessionId=" + sessionId
            );
        }
        return vocabIds;
    }

    // 틀린 것만 학습일 때 단어 목록 가져오기
    private List<Long> getWrongVocabularyIds(Long userId,
                                      Long sessionId,
                                      LearningStartDto.StartRequest req) {

        // 기존 학습 결과 ID (baseResultId) 확인
        Long baseResultId = Optional.ofNullable(req.getBaseResultId())
                .orElseThrow(() -> new InvalidStartParamException("WRONG_ONLY 모드에서는 baseResultId가 필수입니다."));

        LearningResult base = mustLoadLearningResult(baseResultId);

        if (!Objects.equals(base.getUserId(), userId)
                || !Objects.equals(base.getSession().getId(), sessionId)) {
            throw new LearningResultNotFoundException(
                    "해당 사용자의 세션 결과가 아닙니다. userId=%d, sessionId=%d, baseResultId=%d"
                            .formatted(userId, sessionId, baseResultId)
            );
        }

        List<Long> wrongIds = learningResultDetailsRepository
                .findWrongVocabIdsByResultIdOrderBySessionOrder(baseResultId);

        if (wrongIds.isEmpty()) {
            throw new NoWrongVocabularyException(
                    "다시 풀 오답이 없습니다. baseResultId=" + baseResultId
            );
        }
        return wrongIds;
    }


    // 기존 학습 결과 ID(baseResultId)로 결과 조회
    private LearningResult mustLoadLearningResult(Long baseResultId) {
        return learningResultRepository.findById(baseResultId)
                .orElseThrow(() -> new LearningResultNotFoundException(
                        "틀린 것만 학습(wrong_only)시 기준이 되는 결과 id 찾을 수 없습니다. baseResultId=" + baseResultId
                ));
    }

    // 기존 학습 결과 삭제
    private void removeAllResultsFor(Long userId, Long sessionId) {
        List<LearningResult> olds =
                learningResultRepository.findByUserIdAndSessionIds(userId, List.of(sessionId));

        if (olds.isEmpty()) return;

        List<Long> oldIds = olds.stream().map(LearningResult::getId).toList();

        learningResultDetailsRepository.deleteAllByResultIds(oldIds);
        learningResultRepository.deleteAllById(oldIds);
    }

    // 첫 단어 정보 만들기
    private LearningStartDto.StartResponse.FirstVocabulary makeFirstVocabulary(Long vocabId) {
        Vocabulary v = loadVocabulary(vocabId);
        return LearningStartDto.StartResponse.FirstVocabulary.builder()
                .vocabularyId(v.getId())
                .korean(v.getKorean())
                .romanization(v.getRomanized())
                .english(v.getEnglish())
                .imageUrl(v.getImageUrl())
                .build();
    }

    private Vocabulary loadVocabulary(Long vocabularyId) {
        return vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new LearningDataInconsistencyException(
                        "학습에 포함된 단어를 찾을 수 없습니다. vocabularyId=" + vocabularyId
                ));
    }

}