package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, Long> {
    List<Order> findByUserId(Long userId, Sort sort);

    @Query("{ 'userId': ?0, 'status': { $in: ['DELIVERED', 'COMPLETED'] }, 'items.productId': ?1 }")
    List<Order> findPurchasedByUserAndProduct(Long userId, Long productId);
}
