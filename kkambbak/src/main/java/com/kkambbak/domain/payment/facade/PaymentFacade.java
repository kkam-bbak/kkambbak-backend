package com.kkambbak.domain.payment.facade;

import com.kkambbak.client.discord.DiscordClient;
import com.kkambbak.client.mail.service.MailSender;
import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.SubscriptionPlan;
import com.kkambbak.core.entity.user.User;
import com.kkambbak.core.entity.user.enums.AuthProvider;
import com.kkambbak.core.repository.payment.PayHistoryRepository;
import com.kkambbak.domain.payment.dto.PaymentDto;
import com.kkambbak.core.service.UserRoleService;
import com.kkambbak.domain.payment.exception.GuestUserCannotPayException;
import com.kkambbak.domain.payment.exception.PaymentNotFoundException;
import com.kkambbak.domain.payment.service.PaymentService;
import com.kkambbak.domain.payment.service.SubscriptionService;
import com.kkambbak.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class PaymentFacade {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final PayHistoryRepository payHistoryRepository;
    private final DiscordClient discordClient;
    private final UserRoleService userRoleService;
    private final MailSender mailSender;

    public PaymentDto.CreateResponse createPayment(Long userId, Long planId, PaymentDto.CreateRequest request) {
        User user = userRoleService.getUser(userId);
        if (user.getProvider() == AuthProvider.GUEST) {
            throw new GuestUserCannotPayException();
        }

        boolean autoRenew = request != null && Boolean.TRUE.equals(request.getAutoRenew());
        log.info("Creating payment - userId: {}, planId: {}, autoRenew: {}", userId, planId, autoRenew);

        return paymentService.createPayment(userId, planId, autoRenew);
    }

    public String approvePayment(String orderId, String pgToken) {
        try {
            PayHistory payHistory = payHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));

            Long userId = payHistory.getUserId();
            Long paymentId = payHistory.getId();
            boolean autoRenew = Boolean.TRUE.equals(payHistory.getPaymentData().get("autoRenew"));

            paymentService.approvePayment(userId, paymentId, orderId, pgToken, autoRenew);

            SubscriptionPlan premiumPlan = subscriptionService.getPremiumPlan();
            Subscription subscription = subscriptionService.createSubscription(userId, premiumPlan.getId());

            if (autoRenew) {
                String billingKey = (String) payHistory.getPaymentData().get("sid");
                subscription.setBillingKey(billingKey);
                subscription.setAutoRenew(true);
            }

            payHistory.setSubscriptionId(subscription.getId());
            payHistoryRepository.save(payHistory);

            userRoleService.upgradeRole(userId);

            try {
                User user = userRoleService.getUser(userId);
                mailSender.sendPaymentSuccessEmail(
                    user.getEmail(),
                    user.getName(),
                    subscription.getEndDate(),
                    payHistory.getAmount(),
                    payHistory.getPaymentMethod().getDescription(),
                    premiumPlan.getName()
                );
            } catch (Exception emailError) {
                log.warn("Failed to send email notification", emailError);
            }

            discordClient.sendPaymentSuccess(
                userId,
                paymentId,
                payHistory.getPaymentMethod().name(),
                payHistory.getAmount(),
                premiumPlan.getName()
            );

            return paymentService.getApprovalSuccessUrl(orderId);

        } catch (Exception e) {
            log.error("Payment approval failed - orderId: {}, error: {}", orderId, e.getMessage());

            try {
                PayHistory payHistory = payHistoryRepository.findByOrderId(orderId)
                    .orElse(null);
                if (payHistory != null) {
                    payHistory.fail();
                    payHistoryRepository.save(payHistory);

                    SubscriptionPlan premiumPlan = subscriptionService.getPremiumPlan();

                    try {
                        User user = userRoleService.getUser(payHistory.getUserId());
                        mailSender.sendPaymentFailureEmail(
                            user.getEmail(),
                            user.getName(),
                            LocalDateTime.now(),
                            payHistory.getAmount(),
                            payHistory.getPaymentMethod().getDescription(),
                            premiumPlan.getName(),
                            e.getMessage()
                        );
                    } catch (Exception emailError) {
                        log.warn("Failed to send payment failure email", emailError);
                    }

                    discordClient.sendPaymentFailure(
                        payHistory.getUserId(),
                        payHistory.getId(),
                        payHistory.getPaymentMethod().name(),
                        e.getMessage(),
                        premiumPlan.getName()
                    );
                }
            } catch (Exception discordError) {
                log.warn("Failed to send Discord failure notification", discordError);
            }

            return paymentService.getApprovalFailUrl();
        }
    }
}