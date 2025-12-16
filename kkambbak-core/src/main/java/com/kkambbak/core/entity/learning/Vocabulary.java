package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabularies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Vocabulary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String korean;

    @Column(nullable = false, length = 100)
    private String romanized;

    @Column(nullable = false, length = 255)
    private String english;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
}