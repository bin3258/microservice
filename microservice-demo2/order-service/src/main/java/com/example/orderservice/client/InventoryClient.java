package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "Inventory-service", contextId = "inventoryClient")
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve-from-warehouse")
    void reserveFromWarehouse(@RequestBody Map<String, Object> request);

    @PostMapping("/api/inventory/confirm-from-warehouse")
    void confirmFromWarehouse(@RequestBody Map<String, Object> request);

    @PostMapping("/api/inventory/cancel-from-warehouse")
    void cancelFromWarehouse(@RequestBody Map<String, Object> request);

    @PostMapping("/api/inventory/release-from-warehouse")
    void releaseFromWarehouse(@RequestBody Map<String, Object> request);
}