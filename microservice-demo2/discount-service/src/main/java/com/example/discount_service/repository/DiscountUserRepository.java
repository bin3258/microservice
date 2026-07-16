package com.example.discount_service.repository;

import com.example.discount_service.entity.DiscountUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountUserRepository extends JpaRepository<DiscountUser, Long> {
    List<DiscountUser> findByDiscountId(Long discountId);
    List<DiscountUser> findByUserId(Long userId);
    boolean existsByDiscountIdAndUserId(Long discountId, Long userId);
    void deleteByDiscountId(Long discountId);
}
