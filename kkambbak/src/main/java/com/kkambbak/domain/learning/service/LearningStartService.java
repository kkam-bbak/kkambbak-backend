package com.kkambbak.domain.learning.service;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.Vocabulary;
import com.kkambbak.core.repository.learning.LearningResultDetailsRepository;
import com.kkambbak.core.repository.learning.LearningResultRepository;
import com.kkambbak.core.repository.learning.SessionRepository;
import com.kkambbak.core.repository.learning.SessionVocabularyRepository;
import com.kkambbak.core.repository.learning.VocabularyRepository;
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

    private final SessionRepository sessionRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final LearningResultRepository learningResultRepository;
    private final LearningResultDetailsRepository learningResultDetailsRepository;

    //  입력값 검증
    public void validateInputs(Long userId, Long sessionId, LearningStartDto.StartRequest req) {
        if (userId == null || sessionId == null) {
            throw new InvalidStartParamException("userId 또는 sessionId가 null입니다.");
        }
    }

    // 세션 로드
    public Session loadSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "세션을 찾을 수 없습니다. sessionId=" + sessionId
                ));
    }

    // 전체 학습: 세션에 포함된 모든 단어 ID
    public List<Long> getAllVocabularyIds(Long sessionId) {
        List<Long> vocabIds = sessionVocabularyRepository
                .findVocabularyIdsOrderByVocabIdAsc(sessionId);

        if (vocabIds.isEmpty()) {
            throw new LearningDataInconsistencyException(
                    "세션에 연결된 단어가 없습니다. sessionId=" + sessionId
            );
        }
        return vocabIds;
    }

    // 틀린 것만 학습: 오답 단어 ID 목록
    public List<Long> getWrongVocabularyIds(
            Long userId,
            Long sessionId,
            LearningStartDto.StartRequest req
    ) {
        // baseResultId 필수
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

    // 새 LearningResult 생성 (시작 시 호출)
    @Transactional
    public LearningResult createNewResult(Long userId, Session session, int totalCount) {
        return learningResultRepository.save(
                LearningResult.startOf(userId, session, totalCount)
        );
    }

    // 첫 단어 DTO 생성
    public LearningStartDto.StartResponse.FirstVocabulary makeFirstVocabulary(Long vocabId) {
        Vocabulary v = loadVocabulary(vocabId);
        return LearningStartDto.StartResponse.FirstVocabulary.builder()
                .vocabularyId(v.getId())
                .korean(v.getKorean())
                .romanization(v.getRomanized())
                .english(v.getEnglish())
                .imageUrl(v.getImageUrl())
                .build();
    }

    // 기존 결과 로딩 (WRONG_ONLY baseResultId 검증에 사용)
    private LearningResult mustLoadLearningResult(Long baseResultId) {
        return learningResultRepository.findById(baseResultId)
                .orElseThrow(() -> new LearningResultNotFoundException(
                        "틀린 것만 학습(wrong_only)시 기준이 되는 결과 id를 찾을 수 없습니다. baseResultId=" + baseResultId
                ));
    }

    private Vocabulary loadVocabulary(Long vocabularyId) {
        return vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new LearningDataInconsistencyException(
                        "학습에 포함된 단어를 찾을 수 없습니다. vocabularyId=" + vocabularyId
                ));
    }
}