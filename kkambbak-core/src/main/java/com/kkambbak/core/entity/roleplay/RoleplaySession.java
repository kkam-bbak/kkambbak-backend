package com.kkambbak.core.entity.roleplay;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.roleplay.enums.SessionState;
import com.kkambbak.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roleplay_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplaySession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RoleplayScenario scenario;

    private SessionState sessionState;


    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    public boolean isOwner(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    public void endState(){
        this.sessionState=SessionState.END;
        this.completedAt = LocalDateTime.now();
    }

    public static RoleplaySession create(User user, RoleplayScenario scenario) {
        RoleplaySession session = new RoleplaySession();
        session.user = user;
        session.scenario = scenario;
        session.sessionState = SessionState.ACTIVE;
        return session;
    }



}
