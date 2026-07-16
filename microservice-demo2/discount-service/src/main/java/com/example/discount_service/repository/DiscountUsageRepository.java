package com.example.discount_service.repository;

import com.example.discount_service.entity.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Long> {
    List<DiscountUsage> findByDiscountId(Long discountId);
    List<DiscountUsage> findByUserId(Long userId);
    List<DiscountUsage> findByDiscountIdAndUserId(Long discountId, Long userId);
    Optional<DiscountUsage> findByDiscountIdAndUserIdAndOrderId(Long discountId, Long userId, Long orderId);
}
