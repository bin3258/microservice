package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/api/payments/order/{orderId}")
    Map<String, Object> getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}
