package com.kkambbak.domain.learning.service;

import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import com.kkambbak.core.repository.learning.LearningResultRepository;
import com.kkambbak.core.repository.learning.SessionRepository;
import com.kkambbak.core.repository.learning.SessionVocabularyRepository;
import com.kkambbak.domain.learning.dto.LearningSessionListResponse;
import com.kkambbak.domain.learning.dto.SessionCardDto;
import com.kkambbak.domain.learning.exception.InconsistentExposureRuleException;
import com.kkambbak.domain.learning.exception.InvalidPagingParamException;
import com.kkambbak.domain.learning.exception.LearningQueryException;
import com.kkambbak.domain.learning.support.SurveyPrefResolver;
import com.kkambbak.domain.learning.support.SurveyPrefResolver.UserPref;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningListService {
    // limit : 한 페이지에 나타낼 세션의 개수
    private static final int MAX_LIMIT = 50;

    private final SessionRepository sessionRepository;
    private final LearningResultRepository learningResultRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final SurveyPrefResolver surveyPrefResolver;

    public LearningSessionListResponse getLearningList(
            Long userId,
            CategoryType category,
            Long cursor,
            int limit
    ) {
        validatePagingParams(cursor, limit);

        // 설문에서 난이도(difficultyLevel)와 관심사(interestType) 가져오기
        Optional<UserPref> prefOpt = surveyPrefResolver.resolve(userId);

        // 상위노출 세션 ID 목록 조회(일반 세션 목록에서 상위노출 세션이 중복으로 조회되는걸 막기 위해)
        List<Long> excludeIds = getExcludeIds(category, prefOpt);

        // 상위노출 세션 조회
        List<Session> topExposure = fetchTopExposureSessions(
                category, prefOpt, cursor, limit
        );

        // 상위노출을 제외한 나머지 일반 세션 조회
        DefaultSessionResult defaultResult = fetchDefaultSessions(
                category, excludeIds, cursor, limit, topExposure.size()
        );

        // 세션 합치기 (상위 노출 세션 + 일반 세션을 하나의 리스트로 합침)
        List<Session> allSessions = new ArrayList<>(
                topExposure.size() + defaultResult.sessions().size()
        );
        allSessions.addAll(topExposure); // 상위 노출 세션
        allSessions.addAll(defaultResult.sessions()); // 일반 세션

        // 세션이 하나도 없으면 빈 응답 반환
        if (allSessions.isEmpty()) {
            return LearningSessionListResponse.of(category, List.of(), null, false);
        }

        // 세션 ID 목록 추출
        List<Long> sessionIds = allSessions.stream()
                .map(Session::getId)
                .toList();

        // 각 세션의 단어 개수 조회 ( 예 : topik1 = 3개, topik2 = 3개)
        Map<Long, Integer> vocabCountMap = fetchVocabularyCounts(sessionIds);
        // 사용자의 학습 결과 조회 (학습을 완료 했는지, 소요 시간이 얼마나 되는지)
        Map<Long, LearningResult> resultMap = fetchLearningResults(userId, sessionIds);

        // DTO 변환
        List<SessionCardDto> dtos = convertToSessionCards(
                allSessions, vocabCountMap, resultMap
        );

        // 다음 페이지 시작 위치(nextCursor)와 더 있는지(hasNext) 계산
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

    //limit과 cursor 검증
    private void validatePagingParams(Long cursor, int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new InvalidPagingParamException();
        }
        if (cursor != null && cursor <= 0) {
            throw new InvalidPagingParamException();
        }
    }

    // 상위노출 세션 ID 목록 (중복 제거용)
    private List<Long> getExcludeIds(CategoryType category, Optional<UserPref> prefOpt) {
        if (prefOpt.isEmpty()) return null;

        UserPref p = prefOpt.get();
        List<Long> ids =
                sessionRepository.findAllTopExposureIds(category, p.difficultyLevel(), p.interestType());
        return toNullIfEmpty(ids);
    }

    // 상위노출 조회
    private List<Session> fetchTopExposureSessions(
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

    // 상위노출을 제외한 나머지 일반 세션 목록 조회
    private DefaultSessionResult fetchDefaultSessions(
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
    private Map<Long, Integer> fetchVocabularyCounts(List<Long> sessionIds) {
        return sessionVocabularyRepository.findCountsBySessionIds(sessionIds)
                .stream()
                .collect(Collectors.toMap(
                        SessionVocabularyRepository.SessionIdCount::getSessionId,
                        r -> (int) r.getCnt()
                ));
    }

    // 사용자의 학습 결과 조회
    private Map<Long, LearningResult> fetchLearningResults(Long userId, List<Long> sessionIds) {
        if (userId == null) return Map.of();

        return learningResultRepository.findByUserIdAndSessionIds(userId, sessionIds)
                .stream()
                .collect(Collectors.toMap(
                        lr -> lr.getSession().getId(),
                        lr -> lr
                ));
    }

    // 세션 → DTO 변환
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

    // 다음 페이지 시작 위치(nextCursor)와 더 있는지(hasNext) 계산
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

    private record DefaultSessionResult(List<Session> sessions, boolean hasMore) {}
    private record PagingInfo(Long nextCursor, boolean hasNext) {}

    // 빈 리스트를 null로
    private static <T> List<T> toNullIfEmpty(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    // 첫 페이지 요청 객체 생성
    private static PageRequest firstPage(int size) {
        return PageRequest.of(0, size);
    }

    // limit+1개로 다음 페이지 유무 계산
    private record OverfetchResult<T>(List<T> items, boolean hasMore) {
        static <T> OverfetchResult<T> slice(List<T> overFetched, int size) {
            boolean more = overFetched.size() > size;
            List<T> items = more ? overFetched.subList(0, size) : overFetched;
            return new OverfetchResult<>(items, more);
        }
    }
}