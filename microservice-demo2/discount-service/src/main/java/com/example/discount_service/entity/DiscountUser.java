package com.example.discount_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "discount_users", uniqueConstraints = @UniqueConstraint(columnNames = {"discountId", "userId"}))
public class DiscountUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long discountId;

    @Column(nullable = false)
    private Long userId;

    public DiscountUser() {}

    public DiscountUser(Long discountId, Long userId) {
        this.discountId = discountId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDiscountId() { return discountId; }
    public void setDiscountId(Long discountId) { this.discountId = discountId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
