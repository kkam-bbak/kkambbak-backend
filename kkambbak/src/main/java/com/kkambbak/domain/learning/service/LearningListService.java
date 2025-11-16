package com.kkambbak.domain.learning.service;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import com.kkambbak.core.repository.learning.LearningResultRepository;
import com.kkambbak.core.repository.learning.SessionRepository;
import com.kkambbak.core.repository.learning.SessionVocabularyRepository;
import com.kkambbak.domain.learning.exception.InconsistentExposureRuleException;
import com.kkambbak.domain.learning.exception.InvalidPagingParamException;
import com.kkambbak.domain.learning.support.SurveyPrefResolver;
import com.kkambbak.domain.learning.support.SurveyPrefResolver.UserPref;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningListService {

    private static final int MAX_LIMIT = 50;

    private final SessionRepository sessionRepository;
    private final LearningResultRepository learningResultRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final SurveyPrefResolver surveyPrefResolver;

    // limit / cursor 검증
    public void validatePagingParams(Long cursor, int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new InvalidPagingParamException();
        }
        if (cursor != null && cursor <= 0) {
            throw new InvalidPagingParamException();
        }
    }

    // 설문 기반 사용자 선호 조회
    public Optional<UserPref> resolveUserPref(Long userId) {
        return surveyPrefResolver.resolve(userId);
    }

    // 상위노출 세션 ID 목록 (중복 제거용)
    public List<Long> getExcludeIds(CategoryType category, Optional<UserPref> prefOpt) {
        if (prefOpt.isEmpty()) return null;

        UserPref p = prefOpt.get();
        List<Long> ids =
                sessionRepository.findAllTopExposureIds(category, p.difficultyLevel(), p.interestType());
        return toNullIfEmpty(ids);
    }

    // 상위노출 세션 조회
    public List<Session> fetchTopExposureSessions(
            CategoryType categoryType,
            Optional<UserPref> prefOpt,
            Long cursor,
            int limit
    ) {
        if (prefOpt.isEmpty()) return Collections.emptyList();
        if (cursor != null) return Collections.emptyList();

        UserPref p = prefOpt.get();
        List<Session> topExposure =
                sessionRepository.findTopExposureSessions(
                        categoryType,
                        p.difficultyLevel(),
                        p.interestType(),
                        null,
                        firstPage(limit)
                );

        boolean mismatch = topExposure.stream().anyMatch(s -> s.getCategory().getType() != categoryType);
        if (mismatch) throw new InconsistentExposureRuleException();

        return topExposure;
    }

    // 상위노출 제외 일반 세션 조회
    public DefaultSessionResult fetchDefaultSessions(
            CategoryType category,
            List<Long> excludeIds,
            Long cursor,
            int limit,
            int topExposureSize
    ) {
        int remaining = Math.max(0, limit - topExposureSize);
        if (remaining == 0) return new DefaultSessionResult(Collections.emptyList(), false);

        List<Long> finalExclude = toNullIfEmpty(excludeIds);

        List<Session> overFetched =
                sessionRepository.findDefaultSessions(
                        category,
                        finalExclude,
                        cursor,
                        firstPage(remaining + 1)
                );

        OverfetchResult<Session> of = OverfetchResult.slice(overFetched, remaining);

        return new DefaultSessionResult(of.items(), of.hasMore());
    }

    // 세션별 단어 개수 조회
    public Map<Long, Integer> fetchVocabularyCounts(List<Long> sessionIds) {
        return sessionVocabularyRepository.findCountsBySessionIds(sessionIds)
                .stream()
                .collect(Collectors.toMap(
                        SessionVocabularyRepository.SessionIdCount::getSessionId,
                        r -> (int) r.getCnt()
                ));
    }

    // 사용자의 학습 결과 조회 (세션별 최신 1건)
    public Map<Long, LearningResult> fetchLearningResults(Long userId, List<Long> sessionIds) {
        if (userId == null) return Map.of();

        List<LearningResult> results =
                learningResultRepository.findByUserIdAndSessionIds(userId, sessionIds);

        return results.stream()
                .collect(Collectors.toMap(
                        lr -> lr.getSession().getId(),
                        lr -> lr,
                        (oldResult, newResult) ->
                                (oldResult.getId() < newResult.getId())
                                        ? newResult
                                        : oldResult
                ));
    }

    public record DefaultSessionResult(List<Session> sessions, boolean hasMore) {}

    private static <T> List<T> toNullIfEmpty(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    private static PageRequest firstPage(int size) {
        return PageRequest.of(0, size);
    }

    private record OverfetchResult<T>(List<T> items, boolean hasMore) {
        static <T> OverfetchResult<T> slice(List<T> overFetched, int size) {
            boolean more = overFetched.size() > size;
            List<T> items = more ? overFetched.subList(0, size) : overFetched;
            return new OverfetchResult<>(items, more);
        }
    }
}