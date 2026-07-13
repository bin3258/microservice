package com.example.orderservice.client;

import com.example.orderservice.payload.WarehouseInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "Inventory-service", contextId = "warehouseClient")
public interface WarehouseClient {

    @GetMapping("/api/warehouses")
    List<WarehouseInfo> getAllWarehouses();
}
