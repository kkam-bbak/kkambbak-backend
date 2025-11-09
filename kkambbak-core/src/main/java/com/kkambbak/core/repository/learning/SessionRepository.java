package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // 상위노출 세션 조회
    @Query("""
        SELECT s
        FROM Session s
        JOIN SurveyTopExposureRule r ON r.session.id = s.id
        WHERE r.categoryType = :categoryType
          AND r.surveyKey = :surveyKey
          AND r.enabled = true
          AND s.category.type = :categoryType
          AND (:cursor IS NULL OR s.id > :cursor)
        ORDER BY r.exposureScore DESC, s.id ASC
    """)
    List<Session> findTopExposureSessions(
            @Param("categoryType") CategoryType categoryType,
            @Param("surveyKey") String surveyKey,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 상위노출 세션 ID만 전체 조회
    @Query("""
        SELECT s.id
        FROM Session s
        JOIN SurveyTopExposureRule r ON r.session.id = s.id
        WHERE r.categoryType = :categoryType
          AND r.surveyKey = :surveyKey
          AND r.enabled = true
          AND s.category.type = :categoryType
        ORDER BY r.exposureScore DESC, s.id ASC
    """)
    List<Long> findAllTopExposureIds(
            @Param("categoryType") CategoryType categoryType,
            @Param("surveyKey") String surveyKey
    );

    // 일반 세션 조회 (상위노출 제외, 커서 기반 페이징)
    @Query("""
        SELECT s
        FROM Session s
        WHERE s.category.type = :categoryType
          AND (:excludeIds IS NULL OR s.id NOT IN :excludeIds)
          AND (:cursor IS NULL OR s.id > :cursor)
        ORDER BY s.id ASC
    """)
    List<Session> findDefaultSessions(
            @Param("categoryType") CategoryType categoryType,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}