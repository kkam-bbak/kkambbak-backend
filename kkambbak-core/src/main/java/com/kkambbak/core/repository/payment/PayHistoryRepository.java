package com.kkambbak.core.repository.payment;

import com.kkambbak.core.entity.payment.PayHistory;
import com.kkambbak.core.entity.payment.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayHistoryRepository extends JpaRepository<PayHistory, Long> {
    Page<PayHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT p.id, p.user_id, p.subscription_id, p.payment_method, p.amount, p.status, " +
                   "p.transaction_id, p.payment_data, p.paid_at, p.created_at, p.updated_at " +
                   "FROM pay_history p WHERE p.payment_data->>'orderId' = :orderId",
           nativeQuery = true)
    Optional<PayHistory> findByOrderId(@Param("orderId") String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayHistory p WHERE p.userId = :userId AND p.status = :status")
    Optional<PayHistory> findByUserIdAndStatusWithLock(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayHistory p WHERE p.id = :id")
    Optional<PayHistory> findByIdWithLock(@Param("id") Long id);
}