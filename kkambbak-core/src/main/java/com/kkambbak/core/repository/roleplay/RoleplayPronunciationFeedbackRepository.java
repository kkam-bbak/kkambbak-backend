package com.kkambbak.core.repository.roleplay;

import com.kkambbak.core.entity.roleplay.RoleplayPronunciationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleplayPronunciationFeedbackRepository extends JpaRepository<RoleplayPronunciationFeedback, Long> {
    int countByRoleplayDialogue_Id(Long roleplayDialogueId);

    @Query("""
    select count(distinct f.roleplayDialogue.id)
      from RoleplayPronunciationFeedback f
     where f.roleplayDialogue.roleplaySession.id = :sessionId
       and f.result = com.kkambbak.core.entity.roleplay.enums.PronunciationResult.GOOD
""")
    int countGoodDialogues(@Param("sessionId") Long sessionId);

    @Query("""
    select count(distinct f.roleplayDialogue.id)
      from RoleplayPronunciationFeedback f
     where f.roleplayDialogue.roleplaySession.id = :sessionId
""")
    int countAttemptedDialogues(@Param("sessionId") Long sessionId);

    boolean existsByRoleplayDialogue_Id(Long dialogueId);
}
