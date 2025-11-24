package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LearningResult extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;   // 학습 시작 시각

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt; // 학습 완료 시각

    @Column(name = "duration_seconds")
    private Integer durationSeconds;   // 소요 시간(초)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vocabulary_ids", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<Long> vocabularyIds = new ArrayList<>();

    public static LearningResult startOf(Long userId, Session session, List<Long> vocabIds) {
        return LearningResult.builder()
                .userId(userId)
                .session(session)
                .totalCount(vocabIds.size())
                .correctCount(0)
                .startedAt(java.time.LocalDateTime.now())
                .vocabularyIds(vocabIds)
                .build();
    }

    public void complete() {
        this.completedAt = java.time.LocalDateTime.now();
        if (this.startedAt != null) {
            this.durationSeconds =
                    (int) java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    @Transient
    public boolean isCompleted() {
        return this.completedAt != null;
    }

    public void addCorrectCount() {
        if (this.correctCount == null) {
            this.correctCount = 0;
        }
        this.correctCount++;
    }
}