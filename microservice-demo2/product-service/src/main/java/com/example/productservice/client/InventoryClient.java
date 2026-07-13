package com.example.productservice.client;

import com.example.productservice.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory/{productId}")
    InventoryResponse getStock(@PathVariable("productId") Long productId);

    @DeleteMapping("/api/inventory/{productId}")
    void deleteStock(@PathVariable("productId") Long productId);
}
