package com.kkambbak.domain.payment.controller;

import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.facade.PaymentFacade;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/create/{planId}")
    public ApiResponse<PaymentDto.CreateResponse> createPayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long planId
    ) {
        Long userId = userDetails.getUserId();
        PaymentDto.CreateResponse response = paymentFacade.createPayment(userId, planId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/capture")
    public ApiResponse<Void> capturePayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody PaymentDto.CaptureRequest request
    ) {
        Long userId = userDetails.getUserId();
        paymentFacade.capturePayment(userId, request.getPaymentId(), request.getOrderId(), request.getPgToken());
        return ApiResponse.ok();
    }
}