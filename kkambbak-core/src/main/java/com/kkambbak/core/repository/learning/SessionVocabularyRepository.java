package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.SessionVocabulary;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionVocabularyRepository extends JpaRepository<SessionVocabulary, Long> {


    @Query("""
        select sv.session.id as sessionId, count(sv.id) as cnt
          from SessionVocabulary sv
         where sv.session.id in :sessionIds
         group by sv.session.id
    """)
    List<SessionIdCount> findCountsBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    interface SessionIdCount {
        Long getSessionId();
        long getCnt();
    }
}