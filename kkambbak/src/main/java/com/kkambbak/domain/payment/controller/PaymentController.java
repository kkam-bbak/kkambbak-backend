package com.kkambbak.domain.payment.controller;

import com.kkambbak.domain.payment.dto.PaymentDetailDto;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.domain.payment.dto.PaymentResultDto;
import com.kkambbak.domain.payment.facade.PaymentFacade;
import com.kkambbak.domain.payment.service.PaymentService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;

    @PostMapping("/create/{planId}")
    public ApiResponse<PaymentDto.CreateResponse> createPayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long planId,
        @RequestBody PaymentDto.CreateRequest autoRenew
    ) {
        Long userId = userDetails.getUserId();
        PaymentDto.CreateResponse response = paymentFacade.createPayment(userId, planId, autoRenew);
        return ApiResponse.ok(response);
    }

    @GetMapping("/approve")
    public void approvePayment(
        @RequestParam String pg_token,
        @RequestParam String orderId,
        HttpServletResponse response
    ) throws IOException {
        log.info("Payment approval request - orderId: {}, pg_token: {}", orderId, pg_token);
        String redirectUrl = paymentFacade.approvePayment(orderId, pg_token);
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentDetailDto> getPaymentDetail(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long paymentId
    ) {
        Long userId = userDetails.getUserId();
        PaymentDetailDto response = paymentService.getPaymentDetail(userId, paymentId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/list")
    public ApiResponse<PaymentDetailDto.Response> getPaymentList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable
    ) {
        Long userId = userDetails.getUserId();
        Page<PaymentDetailDto> page = paymentService.getPaymentList(userId, pageable);
        return ApiResponse.ok(PaymentDetailDto.Response.from(page));
    }

    @GetMapping("/result")
    public ApiResponse<PaymentResultDto> getPaymentResult(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        PaymentResultDto response = paymentService.getPaymentResult(userId);
        return ApiResponse.ok(response);
    }
}