package com.example.Inventory_service.controller;

import com.example.Inventory_service.dto.InventoryResponse;
import com.example.Inventory_service.dto.ReserveRequest;
import com.example.Inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> getAll() {
        return inventoryService.getAll();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @GetMapping("/{productId}/details")
    public List<InventoryResponse> getProductWarehouseDetails(@PathVariable Long productId) {
        return inventoryService.getProductWarehouseDetails(productId);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryResponse> update(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        Long warehouseId = body.get("warehouseId") != null ? ((Number) body.get("warehouseId")).longValue() : null;
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 0;
        return ResponseEntity.ok(inventoryService.createOrUpdate(productId, warehouseId, quantity));
    }

    @PutMapping("/{productId}/{warehouseId}")
    public ResponseEntity<InventoryResponse> updateByWarehouse(@PathVariable Long productId, @PathVariable Long warehouseId, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(inventoryService.createOrUpdate(productId, warehouseId, body.get("quantity")));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        inventoryService.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/{warehouseId}")
    public ResponseEntity<Void> deleteByWarehouse(@PathVariable Long productId, @PathVariable Long warehouseId) {
        inventoryService.deleteByProductIdAndWarehouseId(productId, warehouseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveRequest request) {
        inventoryService.reserve(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody ReserveRequest request) {
        inventoryService.confirmReservation(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody ReserveRequest request) {
        inventoryService.releaseReservation(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve-from-warehouse")
    public ResponseEntity<Void> reserveFromWarehouse(@Valid @RequestBody ReserveRequest request) {
        inventoryService.reserveFromWarehouse(request.getProductId(), request.getWarehouseId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-from-warehouse")
    public ResponseEntity<Void> confirmFromWarehouse(@Valid @RequestBody ReserveRequest request) {
        inventoryService.confirmFromWarehouse(request.getProductId(), request.getWarehouseId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel-from-warehouse")
    public ResponseEntity<Void> cancelFromWarehouse(@Valid @RequestBody ReserveRequest request) {
        inventoryService.cancelFromWarehouse(request.getProductId(), request.getWarehouseId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release-from-warehouse")
    public ResponseEntity<Void> releaseFromWarehouse(@Valid @RequestBody ReserveRequest request) {
        inventoryService.releaseReservationFromWarehouse(request.getProductId(), request.getWarehouseId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }
}
