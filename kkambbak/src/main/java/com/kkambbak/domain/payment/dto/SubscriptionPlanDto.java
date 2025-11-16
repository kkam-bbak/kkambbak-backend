package com.kkambbak.domain.payment.dto;

import com.kkambbak.core.entity.payment.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionPlanDto {
    private Long id;
    private String name;
    private Long price;

    public static SubscriptionPlanDto from(SubscriptionPlan plan) {
        return SubscriptionPlanDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .build();
    }
}