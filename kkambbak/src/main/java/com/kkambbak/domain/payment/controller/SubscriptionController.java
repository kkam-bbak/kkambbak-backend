package com.kkambbak.domain.payment.controller;

import com.kkambbak.domain.payment.dto.SubscriptionDto;
import com.kkambbak.domain.payment.service.SubscriptionService;
import com.kkambbak.global.response.ApiResponse;
import com.kkambbak.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/active")
    public ApiResponse<SubscriptionDto> getActiveSubscription(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();
        SubscriptionDto subscription = subscriptionService.getActiveSubscription(userId);
        return ApiResponse.ok(subscription);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ApiResponse<String> cancelSubscription(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long subscriptionId
    ) {
        Long userId = userDetails.getUserId();
        subscriptionService.cancelSubscription(userId, subscriptionId);
        return ApiResponse.ok();
    }
}