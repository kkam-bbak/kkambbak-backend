package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_result_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LearningResultDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_result_id", nullable = false)
    private LearningResult learningResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "user_answer", columnDefinition = "TEXT", nullable  = true)
    private String userAnswer;

    public void update(boolean correct, String userAnswer) {
        this.correct = correct;
        this.userAnswer = userAnswer;
    }
}
