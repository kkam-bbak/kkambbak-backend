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

    private static final int MAX_LIMIT = 50;

    private final SessionRepository sessionRepository;
    private final LearningResultRepository learningResultRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;

    public LearningSessionListResponse getLearningList(
            Long userId,
            CategoryType categoryType,
            String surveyKey,
            Long cursor,
            int limit
    ) {
        // 입력 검증
        if (limit <= 0 || limit > MAX_LIMIT) throw new InvalidPagingParamException();
        if (cursor != null && cursor < 0)      throw new InvalidPagingParamException();

        final String normalizedKey =
                (surveyKey == null || surveyKey.isBlank()) ? null : surveyKey.trim().toUpperCase();

        // 상위 노출 관련
        List<Session> topExposure = Collections.emptyList(); // 첫 페이지 상위 노출 카드
        List<String> excludeSlugs;

        try {
            // 설문 키가 있으면 제외용 slug 전체를 미리 조회 (모든 페이지에서 사용)
            if (normalizedKey != null) {
                excludeSlugs = sessionRepository.findAllTopExposureSlugs(categoryType, normalizedKey);
                if (excludeSlugs != null && excludeSlugs.isEmpty()) {
                    excludeSlugs = null;
                }
            } else {
                excludeSlugs = null;
            }

            // 첫 페이지 + 설문키 존재 → 상위 노출 카드 실제로 뿌릴 데이터 조회
            if (cursor == null && normalizedKey != null) {
                topExposure = sessionRepository.findTopExposureSessions(
                        categoryType, normalizedKey, PageRequest.of(0, limit)
                );

                // 규칙-세션 카테고리 불일치 방어
                boolean mismatch = topExposure.stream()
                        .anyMatch(s -> s.getCategory().getType() != categoryType);
                if (mismatch) throw new InconsistentExposureRuleException();
            }
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }

        // 기본 목록(상위노출 제외, 커서 기반)
        List<Session> defaults;
        boolean hasMoreDefaults = false; // 기본 목록 다음 페이지 여부
        try {
            int remaining = Math.max(0, limit - topExposure.size());

            if (remaining > 0) {
                // 다음 페이지 존재 여부 판단을 위해 remaining+1개 조회
                List<Session> overFetched = sessionRepository.findDefaultSessions(
                        categoryType,
                        // 첫 페이지 뿐 아니라 모든 페이지에서 상위노출 전체 slug 제외
                        (excludeSlugs == null || excludeSlugs.isEmpty()) ? null : excludeSlugs,
                        cursor,
                        PageRequest.of(0, remaining + 1)
                );
                hasMoreDefaults = overFetched.size() > remaining;
                defaults = (overFetched.size() > remaining)
                        ? overFetched.subList(0, remaining)
                        : overFetched;
            } else {
                defaults = Collections.emptyList();
            }
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }

        // 상위노출 목록과 기본 목록 합치기
        List<Session> all = new ArrayList<>(topExposure.size() + defaults.size());
        all.addAll(topExposure);
        all.addAll(defaults);

        if (all.isEmpty()) {
            // 아무 것도 없으면 커서/hasNext도 기본값으로 응답
            return LearningSessionListResponse.of(categoryType, List.of(), null, false);
        }

        // 단어 수 일괄 카운트
        Map<Long, Integer> vocabCountMap;
        List<Long> sessionIds = all.stream().map(Session::getId).toList();
        try {
            vocabCountMap = sessionVocabularyRepository.findCountsBySessionIds(sessionIds).stream()
                    .collect(Collectors.toMap(
                            SessionVocabularyRepository.SessionIdCount::getSessionId,
                            r -> (int) r.getCnt()
                    ));
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }

        // 사용자 학습결과 병합(완료/소요시간)
        Map<Long, LearningResult> resultMap;
        try {
            resultMap = (userId == null)
                    ? Map.of()
                    : learningResultRepository.findByUserIdAndSessionIds(userId, sessionIds)
                    .stream()
                    .collect(Collectors.toMap(lr -> lr.getSession().getId(), lr -> lr));
        } catch (DataAccessException e) {
            throw new LearningQueryException();
        }

        // DTO 변환
        List<SessionCardDto> dtos = all.stream()
                .map(s -> {
                    int vocabCount = vocabCountMap.getOrDefault(s.getId(), 0);
                    LearningResult lr = resultMap.get(s.getId());
                    return SessionCardDto.of(s, lr, vocabCount);
                })
                .toList();

        // nextCursor / hasNext 계산
        Long nextCursor = null;
        boolean hasNext = false;

        if (!defaults.isEmpty()) {
            // 기본 목록이 일부라도 내려갔다면 -> 마지막 기본목록 id를 커서로
            nextCursor = defaults.get(defaults.size() - 1).getId();
            hasNext = hasMoreDefaults;
        } else {
            // 기본 목록이 한 건도 안 내려간 경우(=상위노출만 내려간 경우)
            try {
                List<Session> peek = sessionRepository.findDefaultSessions(
                        categoryType,
                        (excludeSlugs == null || excludeSlugs.isEmpty()) ? null : excludeSlugs,
                        null,
                        PageRequest.of(0, 1)
                );
                if (!peek.isEmpty()) {
                    nextCursor = peek.get(0).getId();
                    hasNext = true;
                } else {
                    nextCursor = null;
                    hasNext = false;
                }
            } catch (DataAccessException e) {
                throw new LearningQueryException();
            }
        }

        // 응답
        return LearningSessionListResponse.of(categoryType, dtos, nextCursor, hasNext);
    }
}