package com.kkambbak.domain.learning.service;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.LearningResultDetail;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.Vocabulary;
import com.kkambbak.core.repository.learning.LearningResultDetailsRepository;
import com.kkambbak.core.repository.learning.LearningResultRepository;
import com.kkambbak.domain.learning.dto.LearningResultDto;
import com.kkambbak.domain.learning.exception.LearningDataInconsistencyException;
import com.kkambbak.domain.learning.exception.ResultNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningResultQueryService {

    private final LearningResultRepository learningResultRepository;
    private final LearningResultDetailsRepository detailsRepository;

    // 학습 결과 요약
    public LearningResultDto.SummaryResponse getLatestSummary(Long userId, Long sessionId) {

        LearningResult result = loadLatestResult(userId, sessionId);
        Session session = result.getSession();

        if (result.getDurationSeconds() == null || result.getCompletedAt() == null) {
            throw new LearningDataInconsistencyException(
                    "durationSeconds 또는 completedAt 값이 NULL입니다. " +
                            "userId=%d, sessionId=%d, resultId=%d"
                                    .formatted(userId, sessionId, result.getId())
            );
        }

        return LearningResultDto.SummaryResponse.builder()
                .sessionId(session.getId())
                .resultId(result.getId())
                .sessionTitle(session.getTitle())
                .totalCount(result.getTotalCount())
                .correctCount(result.getCorrectCount())
                .durationSeconds(result.getDurationSeconds())
                .completedAt(result.getCompletedAt())
                .build();
    }


    // 학습 결과 요약 및 정오답 리스트
    public LearningResultDto.ReviewResponse getLatestReview(Long userId, Long sessionId) {

        LearningResult result = loadLatestResult(userId, sessionId);
        Session session = result.getSession();

        if (result.getDurationSeconds() == null || result.getCompletedAt() == null) {
            throw new LearningDataInconsistencyException(
                    "durationSeconds 또는 completedAt 값이 NULL입니다. " +
                            "userId=%d, sessionId=%d, resultId=%d"
                                    .formatted(userId, sessionId, result.getId())
            );
        }

        List<LearningResultDetail> details =
                detailsRepository.findByLearningResultId(result.getId());

        details.sort(Comparator.comparing(d -> d.getVocabulary().getId()));

        List<LearningResultDto.ReviewItem> items = details.stream()
                .map(detail -> {
                    Vocabulary v = detail.getVocabulary();
                    return LearningResultDto.ReviewItem.builder()
                            .vocabularyId(v.getId())
                            .korean(v.getKorean())
                            .romanization(v.getRomanized())
                            .english(v.getEnglish())
                            .correct(detail.isCorrect())
                            .build();
                })
                .toList();

        LearningResultDto.SummaryResponse summary =
                LearningResultDto.SummaryResponse.builder()
                        .sessionId(session.getId())
                        .resultId(result.getId())
                        .sessionTitle(session.getTitle())
                        .totalCount(result.getTotalCount())
                        .correctCount(result.getCorrectCount())
                        .durationSeconds(result.getDurationSeconds())
                        .completedAt(result.getCompletedAt())
                        .build();

        return LearningResultDto.ReviewResponse.builder()
                .summary(summary)
                .items(items)
                .build();
    }


    private LearningResult loadLatestResult(Long userId, Long sessionId) {

        List<LearningResult> results =
                learningResultRepository.findByUserIdAndSessionIds(userId, List.of(sessionId));

        return results.stream()
                .max(Comparator.comparingLong(LearningResult::getId))
                .orElseThrow(() -> new ResultNotFoundException(
                        "학습 결과를 찾을 수 없습니다. userId=%d, sessionId=%d"
                                .formatted(userId, sessionId)
                ));
    }
}