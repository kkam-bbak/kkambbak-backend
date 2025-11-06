package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 세션 목록 조회용 레포지토리
 */
public interface SessionRepository extends JpaRepository<Session, Long> {

    // 상위노출: 점수 내림차순, slug 오름차순
    @Query("""
        select s
          from Session s
          join SurveyTopExposureRule r
            on r.sessionSlug = s.slug
         where r.categoryType = :categoryType
           and r.surveyKey     = :surveyKey
           and r.enabled       = true
           and s.category.type = :categoryType
         order by r.exposureScore desc, s.slug asc
    """)
    List<Session> findTopExposureSessions(@Param("categoryType") CategoryType categoryType,
                                          @Param("surveyKey") String surveyKey,
                                          Pageable limitOnly);

    //해당 설문키의 상위노출 세션 slug 전체를 반환 (페이징 없음, 제외 목록용)
    @Query("""
        select s.slug
          from Session s
          join SurveyTopExposureRule r
            on r.sessionSlug = s.slug
         where r.categoryType = :categoryType
           and r.surveyKey     = :surveyKey
           and r.enabled       = true
           and s.category.type = :categoryType
    """)
    List<String> findAllTopExposureSlugs(@Param("categoryType") CategoryType categoryType,
                                         @Param("surveyKey") String surveyKey);

    /**
     * 기본목록: 상위노출 제외, 커서 이후 id 오름차순
     * - excludedSlugs가 비었으면 null로 넘겨야 JPQL에서 조건이 생략됨
     */
    @Query("""
        select s
          from Session s
         where s.category.type = :categoryType
           and (:excludedSlugs is null or s.slug not in :excludedSlugs)
           and (:cursor is null or s.id > :cursor)
         order by s.id asc
    """)
    List<Session> findDefaultSessions(@Param("categoryType") CategoryType categoryType,
                                      @Param("excludedSlugs") List<String> excludedSlugs,
                                      @Param("cursor") Long cursor,
                                      Pageable limitOnly);
}