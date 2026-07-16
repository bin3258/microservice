package com.example.Payment_service.client;

import com.example.Payment_service.dto.OrderStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {
    @PutMapping("/api/orders/{id}/status")
    void updateOrderStatus(@PathVariable("id") Long orderId, @RequestBody OrderStatusRequest request);

    @PutMapping("/api/orders/{id}/confirm-payment")
    void confirmPayment(@PathVariable("id") Long orderId);
}
