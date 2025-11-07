package com.kkambbak.core.repository.payment;

import com.kkambbak.core.entity.payment.Subscription;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s " +
           "WHERE CAST(s.endDate AS DATE) = CAST(:targetDate AS DATE) " +
           "AND s.status = :status " +
           "AND s.autoRenew = true " +
           "AND s.billingKey IS NOT NULL")
    List<Subscription> findDueForRenewal(@Param("targetDate") LocalDateTime targetDate,
                                          @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s " +
           "WHERE s.endDate < :now " +
           "AND s.status = :status")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now,
                                                @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s " +
           "WHERE CAST(s.endDate AS DATE) = CAST(:targetDate AS DATE) " +
           "AND s.status = :status " +
           "AND s.autoRenew = true")
    List<Subscription> findSubscriptionsExpiringOn(@Param("targetDate") LocalDateTime targetDate,
                                                   @Param("status") SubscriptionStatus status);
}
