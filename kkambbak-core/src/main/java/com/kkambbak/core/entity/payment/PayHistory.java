package com.kkambbak.core.entity.payment;

import com.kkambbak.core.entity.BaseEntity;
import com.kkambbak.core.entity.payment.enums.PaymentMethod;
import com.kkambbak.core.entity.payment.enums.PaymentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Entity
@Table(name = "pay_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class PayHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> paymentData = new HashMap<>();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public void complete(String transactionId, Map<String, Object> paymentData, Long subscriptionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentData = paymentData;
        this.subscriptionId = subscriptionId;
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}