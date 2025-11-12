package com.kkambbak.core.entity.roleplay;

import com.kkambbak.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roleplay_scenario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplayScenario extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title",length = 255, nullable = false)
    private String title;

    @Column(name="description",columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer estimated_minutes;
}
