package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.LearningResult;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningResultRepository extends JpaRepository<LearningResult, Long> {

    /** 사용자 + 세션목록 기준으로 한 번에 조회  */
    @Query("""
        select lr
          from LearningResult lr
         where lr.userId = :userId
           and lr.session.id in :sessionIds
    """)
    List<LearningResult> findByUserIdAndSessionIds(@Param("userId") Long userId,
                                                   @Param("sessionIds") List<Long> sessionIds);

}