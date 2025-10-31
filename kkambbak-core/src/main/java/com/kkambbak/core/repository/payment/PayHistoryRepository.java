package com.kkambbak.core.repository.payment;

import com.kkambbak.core.entity.payment.PayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayHistoryRepository extends JpaRepository<PayHistory, Long> {
    List<PayHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<PayHistory> findByTransactionId(String transactionId);
}