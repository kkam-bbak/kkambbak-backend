package com.kkambbak.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResultDto {
    private String userName;
    private String userEmail;
    private String planName;
}