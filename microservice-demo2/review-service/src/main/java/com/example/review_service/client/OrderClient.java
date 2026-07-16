package com.example.review_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/user/{userId}/has-purchased/{productId}")
    Map<String, Boolean> hasPurchased(@PathVariable("userId") Long userId, @PathVariable("productId") Long productId);
}
