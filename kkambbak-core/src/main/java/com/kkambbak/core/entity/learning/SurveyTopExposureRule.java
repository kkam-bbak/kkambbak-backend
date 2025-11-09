package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "survey_top_exposure_rule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_type", "survey_key", "session_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SurveyTopExposureRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 20)
    private CategoryType categoryType; // TOPIK / CASUAL

    @Column(name = "survey_key", nullable = false, length = 50)
    private String surveyKey;          // 예: "BEGINNER", "FOOD_AND_TRAVEL"

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "exposure_score", nullable = false)
    private Integer exposureScore;     // 숫자 클수록 상위 노출

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    public void enable()  { this.enabled = true; }
    public void disable() { this.enabled = false; }

}