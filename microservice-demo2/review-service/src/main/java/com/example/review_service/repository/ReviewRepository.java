package com.example.review_service.repository;

import com.example.review_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(Long productId);

    Optional<Review> findByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = ?1 AND r.isActive = true")
    long countByProductId(Long productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.productId = ?1 AND r.isActive = true")
    Double averageRatingByProductId(Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = ?1 AND r.isActive = true GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> ratingDistributionByProductId(Long productId);
}
