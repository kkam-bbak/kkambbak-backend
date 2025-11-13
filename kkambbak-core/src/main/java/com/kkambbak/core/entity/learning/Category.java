package com.kkambbak.core.entity.learning;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.survey.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Category extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CategoryType type; // TOPIK / CASUAL

    public static Category of(CategoryType type) {
        return Category.builder().type(type).build();
    }
}