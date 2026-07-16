package com.example.discount_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_usages")
public class DiscountUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long discountId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Double discountAmount;

    @Column(updatable = false)
    private LocalDateTime usedAt = LocalDateTime.now();

    public DiscountUsage() {}

    public DiscountUsage(Long discountId, Long userId, Long orderId, Double discountAmount) {
        this.discountId = discountId;
        this.userId = userId;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDiscountId() { return discountId; }
    public void setDiscountId(Long discountId) { this.discountId = discountId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
