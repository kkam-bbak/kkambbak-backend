package com.kkambbak.core.repository.payment;

import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    Optional<Subscription> findByUserId(Long userId);
}
