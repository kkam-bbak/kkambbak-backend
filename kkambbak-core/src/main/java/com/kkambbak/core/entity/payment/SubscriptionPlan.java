package com.kkambbak.core.entity.payment;

import com.kkambbak.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "subscription_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer durationDays;
}