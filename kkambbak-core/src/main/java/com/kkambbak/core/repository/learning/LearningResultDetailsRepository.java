package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.LearningResultDetail;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningResultDetailsRepository extends JpaRepository<LearningResultDetail, Long> {

    // 틀리 단어 ID를 세션 ID ASC 순서대로 가져오기
    @Query("""
    select sv.vocabulary.id
      from LearningResultDetail d
      join d.learningResult lr
      join SessionVocabulary sv
           on sv.session.id = lr.session.id
          and sv.vocabulary.id = d.vocabulary.id
     where d.learningResult.id = :resultId
       and d.correct = false
     order by sv.vocabulary.id asc
""")
    List<Long> findWrongVocabIdsByResultIdOrderBySessionOrder(@Param("resultId") Long resultId);

    // 학습 초기화
    @Modifying
    @Query("delete from LearningResultDetail d where d.learningResult.id in :ids")
    int deleteAllByResultIds(@Param("ids") List<Long> ids);
}
