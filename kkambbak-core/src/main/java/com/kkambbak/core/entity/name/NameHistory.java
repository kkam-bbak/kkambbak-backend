package com.kkambbak.core.entity.name;


import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.user.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "name_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NameHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> generationOutput;

    private Integer attemptCount;

    private Boolean isSelected;


    public static NameHistory create(User user, Map<String, Object> output, Integer attemptCount) {
        NameHistory history = new NameHistory();
        history.user = user;
        history.generationOutput = output;
        history.attemptCount = attemptCount;
        history.isSelected = false;
        return history;
    }


    public void markSelected() {
        this.isSelected = true;
    }

}
