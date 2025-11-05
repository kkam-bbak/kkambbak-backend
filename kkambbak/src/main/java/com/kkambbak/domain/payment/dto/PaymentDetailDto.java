package com.kkambbak.domain.payment.dto;

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
public class PaymentDetailDto {
    private Long paymentId;
    private String userName;
    private String userEmail;
    private String planName;
    private Long amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    public static class Response {
        private Long totalElements;
        private Integer numberOfElements;
        private List<PaymentDetailDto> payments;

        public static Response from(Page<PaymentDetailDto> page) {
            return Response.builder()
                    .totalElements(page.getTotalElements())
                    .numberOfElements(page.getNumberOfElements())
                    .payments(page.getContent())
                    .build();
        }
    }
}