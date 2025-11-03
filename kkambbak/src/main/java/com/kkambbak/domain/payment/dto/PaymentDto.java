package com.kkambbak.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class PaymentDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateResponse {
        private Long paymentId;
        private String orderId;
        private String approvalUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CaptureRequest {
        private Long paymentId;
        private String orderId;
        @JsonProperty("pg_token")
        private String pgToken;
    }
}