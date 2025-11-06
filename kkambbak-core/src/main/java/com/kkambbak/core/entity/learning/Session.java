package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.survey.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Session extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** TOPIK / CASUAL 등의 상위 분류  */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    /** 불변 식별자 (API·규칙·URL에서 사용) */
    @Column(nullable = false, length = 100, unique = true)
    private String slug;   // 예: "topik_1", "emotions"

    /** 사용자에게 보일 이름 (현지화/카피 수정 가능) */
    @Column(nullable = false, length = 255)
    private String title;  // 예: "Topik 1", "Emotions"

    /** TOPIK에만 쓰는 난이도, CASUAL은 null 허용 */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private DifficultyLevel difficulty;


}