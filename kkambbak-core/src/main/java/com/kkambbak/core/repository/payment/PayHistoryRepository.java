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

public interface PayHistoryRepository extends JpaRepository<PayHistory, Long> {
    List<PayHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PayHistory> findByTransactionId(String transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayHistory p WHERE p.userId = :userId AND p.status = :status")
    Optional<PayHistory> findByUserIdAndStatusWithLock(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayHistory p WHERE p.id = :id")
    Optional<PayHistory> findByIdWithLock(@Param("id") Long id);
}