package com.kkambbak.core.entity.roleplay;

import com.fasterxml.jackson.databind.JsonNode;
import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "roleplay_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplayResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,foreignKey = @ForeignKey(name="fk_result_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false,foreignKey = @ForeignKey(name="fk_result_scenario"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoleplayScenario scenario;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_feedback",columnDefinition = "jsonb")
    private JsonNode aiFeedback;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

}
