package com.kkambbak.core.entity.roleplay;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.roleplay.enums.SpeakerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "roleplay_dialogues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleplayDialogues extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false,foreignKey = @ForeignKey(name="fk_dialogues_scenario"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoleplayScenario scenario;


    @Column(nullable = false)
    private SpeakerType speakerType;

    @Column(name = "korean", columnDefinition = "TEXT", nullable = false)
    private String korean;

    @Column(name = "romanized", columnDefinition = "TEXT", nullable = false)
    private String romanized;

    @Column(name = "english", columnDefinition = "TEXT", nullable = false)
    private String english;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(nullable = false)
    private int displayOrder;
}
