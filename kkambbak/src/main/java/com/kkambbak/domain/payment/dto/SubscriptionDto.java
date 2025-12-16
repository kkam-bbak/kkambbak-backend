package com.kkambbak.domain.payment.dto;

import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionDto {
    private Long subscriptionId;
    private String planName;
    private Long planAmount;
    private Integer planDurationDays;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private Boolean hasActiveBillingKey;
    
    public static SubscriptionDto from(Subscription subscription) {
        return SubscriptionDto.builder()
                .subscriptionId(subscription.getId())
                .planName(subscription.getPlan().getName())
                .planAmount(subscription.getPlan().getPrice())
                .planDurationDays(subscription.getPlan().getDurationDays())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .hasActiveBillingKey(subscription.getBillingKey() != null && !subscription.getBillingKey().isEmpty())
                .build();
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    public static class Response {
        private Long totalElements;
        private Integer numberOfElements;
        private List<SubscriptionDto> subscriptions;

        public static Response from(Page<SubscriptionDto> page) {
            return Response.builder()
                    .totalElements(page.getTotalElements())
                    .numberOfElements(page.getNumberOfElements())
                    .subscriptions(page.getContent())
                    .build();
        }
    }
}