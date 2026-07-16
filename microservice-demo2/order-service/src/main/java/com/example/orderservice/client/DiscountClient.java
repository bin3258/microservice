package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "discount-service")
public interface DiscountClient {

    @PostMapping("/api/discounts/validate")
    Map<String, Object> validate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/discounts/mark-used")
    void markUsed(@RequestBody Map<String, Object> request);

    @PostMapping("/api/discounts/release")
    void release(@RequestBody Map<String, Object> request);
}
