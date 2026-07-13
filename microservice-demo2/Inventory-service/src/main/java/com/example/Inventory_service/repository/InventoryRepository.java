package com.example.Inventory_service.repository;

import com.example.Inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    void deleteByProductId(Long productId);
    void deleteByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
