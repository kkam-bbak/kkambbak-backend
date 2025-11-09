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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public LearningSessionListResponse getLearningList(
            Long userId,
            CategoryType category,
            String surveyKey,
            Long cursor,
            int limit
    ) {
        //limit과 cursor 검증
        validatePagingParams(cursor, limit);

        // 설문키(surveyKey) 정규화 (null/공백 처리, 대문자 변환)
        String normalizedKey = normalizeSurveyKey(surveyKey);

        // 상위노출 세션 ID 목록 조회(일반 세션 목록에서 상위노출 세션이 중복으로 조회되는걸 막기 위해)
        List<Long> excludeIds = getExcludeIds(category, normalizedKey);

        // 상위노출 세션 조회
        List<Session> topExposure = fetchTopExposureSessions(
                category, normalizedKey, cursor, limit
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
        if (cursor != null && cursor < 0) {
            throw new InvalidPagingParamException();
        }
    }

    // 설문키(surveyKey) 정규화 (null/공백 처리, 대문자 변환)
    private String normalizeSurveyKey(String surveyKey) {
        return (surveyKey == null || surveyKey.isBlank())
                ? null
                : surveyKey.trim().toUpperCase();
    }

    // 상위노출 세션 ID 목록 조회(일반 세션 목록에서 상위노출 세션이 중복으로 조회되는걸 막기 위해)
    private List<Long> getExcludeIds(CategoryType category, String normalizedKey) {

        if (normalizedKey == null) {
            return null;
        }

        try {
            List<Long> excludeIds = sessionRepository.findAllTopExposureIds(
                    category, normalizedKey
            );


            List<Long> result = (excludeIds != null && excludeIds.isEmpty()) ? null : excludeIds;

            return result;
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }
    }


    // 상위노출 세션 조회
    private List<Session> fetchTopExposureSessions(
            CategoryType categoryType,
            String normalizedKey,
            Long cursor,
            int limit
    ) {
        // 설문키 없으면 빈 리스트
        if (normalizedKey == null) {
            return Collections.emptyList();
        }

        if(cursor != null) {
            return Collections.emptyList();
        }

        try {
            List<Session> topExposure = sessionRepository.findTopExposureSessions(
                    categoryType,
                    normalizedKey,
                    null,
                    PageRequest.of(0, limit)
            );

            // 카테고리 불일치 방어
            boolean mismatch = topExposure.stream()
                    .anyMatch(s -> s.getCategory().getType() != categoryType);

            if (mismatch) {
                throw new InconsistentExposureRuleException();
            }

            return topExposure;
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }
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

        if (remaining == 0) {
            return new DefaultSessionResult(Collections.emptyList(), false);
        }

        try {
            List<Long> finalExcludeIds = (excludeIds == null || excludeIds.isEmpty()) ? null : excludeIds;

            List<Session> overFetched = sessionRepository.findDefaultSessions(
                    category,
                    finalExcludeIds,
                    cursor,
                    PageRequest.of(0, remaining + 1)
            );


            boolean hasMore = overFetched.size() > remaining;

            List<Session> sessions = hasMore
                    ? overFetched.subList(0, remaining)
                    : overFetched;

            return new DefaultSessionResult(sessions, hasMore);
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }
    }
    // 세션별 단어 개수 조회
    private Map<Long, Integer> fetchVocabularyCounts(List<Long> sessionIds) {
        try {
            return sessionVocabularyRepository.findCountsBySessionIds(sessionIds)
                    .stream()
                    .collect(Collectors.toMap(
                            SessionVocabularyRepository.SessionIdCount::getSessionId,
                            r -> (int) r.getCnt()
                    ));
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }
    }

    // 사용자의 학습 결과 조회
    private Map<Long, LearningResult> fetchLearningResults(Long userId, List<Long> sessionIds) {
        if (userId == null) {
            return Map.of();
        }

        try {
            return learningResultRepository.findByUserIdAndSessionIds(userId, sessionIds)
                    .stream()
                    .collect(Collectors.toMap(
                            lr -> lr.getSession().getId(),
                            lr -> lr
                    ));
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }
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

    /**
     * 일반 세션 조회 결과
     */
    private record DefaultSessionResult(
            List<Session> sessions,
            boolean hasMore
    ) {}

    /**
     * 페이징 정보
     */
    private record PagingInfo(
            Long nextCursor,
            boolean hasNext
    ) {}
}