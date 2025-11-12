package com.kkambbak.core.repository.roleplay;

import com.kkambbak.core.entity.roleplay.RoleplayDialogues;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleplayDialoguesRepository extends JpaRepository<RoleplayDialogues,Long> {

    Optional<RoleplayDialogues> findTopByRoleplaySessionIdOrderByTurnIndexDesc(Long sessionId);

    @Query("select max(d.turnIndex) from RoleplayDialogues d where d.roleplaySession.id = :sessionId")
    Integer findMaxTurnIndexBySessionId(Long sessionId);
    int countByRoleplaySession_IdAndSpeakerType(Long sessionId, SpeakerType speakerType);
}
