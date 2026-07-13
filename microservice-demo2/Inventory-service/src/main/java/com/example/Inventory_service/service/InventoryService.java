package com.example.Inventory_service.service;

import com.example.Inventory_service.dto.InventoryResponse;
import com.example.Inventory_service.dto.ReserveRequest;
import com.example.Inventory_service.entity.Inventory;
import com.example.Inventory_service.entity.Warehouse;
import com.example.Inventory_service.repository.InventoryRepository;
import com.example.Inventory_service.repository.WarehouseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    private final JdbcTemplate jdbcTemplate;
    private Long defaultWarehouseId;

    public InventoryService(InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository, DataSource dataSource) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void init() {
        migrateSchema();

        if (warehouseRepository.count() == 0) {
            Warehouse def = warehouseRepository.save(new Warehouse("Kho mặc định", ""));
            defaultWarehouseId = def.getId();
        } else {
            defaultWarehouseId = warehouseRepository.findAll().get(0).getId();
        }

        List<Inventory> all = inventoryRepository.findAll();
        boolean needsMigrate = all.stream().anyMatch(inv -> inv.getWarehouseId() == null);
        if (needsMigrate) {
            for (Inventory inv : all) {
                if (inv.getWarehouseId() == null) {
                    inv.setWarehouseId(defaultWarehouseId);
                    inventoryRepository.save(inv);
                }
            }
        }
    }

    private void migrateSchema() {
        try {
            List<String> indexes = jdbcTemplate.query(
                "SHOW INDEX FROM inventory WHERE Non_unique = 0 AND Key_name != 'PRIMARY'",
                (rs, rowNum) -> rs.getString("Key_name")
            );
            for (String idx : indexes) {
                jdbcTemplate.execute("ALTER TABLE inventory DROP INDEX `" + idx + "`");
            }
        } catch (Exception e) {
            // schema may already be clean; ignore
        }
        try {
            jdbcTemplate.execute("ALTER TABLE inventory ADD CONSTRAINT UK_product_warehouse UNIQUE (productId, warehouseId)");
        } catch (Exception e) {
            // constraint may already exist; ignore
        }
    }

    public List<InventoryResponse> getAll() {
        List<Inventory> all = inventoryRepository.findAll();
        List<InventoryResponse> result = new ArrayList<>();
        for (Inventory inv : all) {
            result.add(toResponse(inv));
        }
        return result;
    }

    public InventoryResponse getByProductId(Long productId) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tồn kho cho sản phẩm " + productId);
        }
        int totalQty = 0;
        int totalReserved = 0;
        for (Inventory inv : records) {
            totalQty += inv.getQuantity();
            totalReserved += inv.getReservedQuantity();
        }
        return new InventoryResponse(null, productId, null, null, totalQty, totalReserved, totalQty - totalReserved);
    }

    public List<InventoryResponse> getProductWarehouseDetails(Long productId) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        List<InventoryResponse> result = new ArrayList<>();
        for (Inventory inv : records) {
            result.add(toResponse(inv));
        }
        return result;
    }

    @Transactional
    public InventoryResponse createOrUpdate(Long productId, Long warehouseId, Integer quantity) {
        if (warehouseId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn kho hàng");
        }
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElse(new Inventory(productId, warehouseId, 0));
        inv.setProductId(productId);
        inv.setWarehouseId(warehouseId);
        inv.setQuantity(quantity);
        return toResponse(inventoryRepository.save(inv));
    }

    @Transactional
    public void reserve(ReserveRequest request) {
        List<Inventory> records = inventoryRepository.findByProductId(request.getProductId());
        int totalAvailable = records.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        if (totalAvailable < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sản phẩm " + request.getProductId() + " không đủ hàng. Còn: " + totalAvailable);
        }
        int remaining = request.getQuantity();
        for (Inventory inv : records) {
            if (remaining <= 0) break;
            int avail = inv.getAvailableQuantity();
            int take = Math.min(avail, remaining);
            inv.setReservedQuantity(inv.getReservedQuantity() + take);
            remaining -= take;
            inventoryRepository.save(inv);
        }
    }

    @Transactional
    public void confirmReservation(Long productId, Integer quantity) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        int remaining = quantity;
        for (Inventory inv : records) {
            if (remaining <= 0) break;
            int reserved = inv.getReservedQuantity();
            int take = Math.min(reserved, remaining);
            inv.setQuantity(inv.getQuantity() - take);
            inv.setReservedQuantity(inv.getReservedQuantity() - take);
            remaining -= take;
            inventoryRepository.save(inv);
        }
    }

    @Transactional
    public void releaseReservation(Long productId, Integer quantity) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        int remaining = quantity;
        for (Inventory inv : records) {
            if (remaining <= 0) break;
            int reserved = inv.getReservedQuantity();
            int release = Math.min(reserved, remaining);
            inv.setReservedQuantity(inv.getReservedQuantity() - release);
            remaining -= release;
            inventoryRepository.save(inv);
        }
    }

    @Transactional
    public void reserveFromWarehouse(Long productId, Long warehouseId, Integer quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
        if (inv.getAvailableQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sản phẩm " + productId + " tại kho không đủ hàng. Còn: " + inv.getAvailableQuantity());
        }
        inv.setReservedQuantity(inv.getReservedQuantity() + quantity);
        inventoryRepository.save(inv);
    }

    @Transactional
    public void confirmFromWarehouse(Long productId, Long warehouseId, Integer quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
        if (inv.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sản phẩm " + productId + " tại kho không đủ hàng. Còn: " + inv.getQuantity());
        }
        inv.setQuantity(inv.getQuantity() - quantity);
        inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
        inventoryRepository.save(inv);
    }

    @Transactional
    public void releaseReservationFromWarehouse(Long productId, Long warehouseId, Integer quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
        inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
        inventoryRepository.save(inv);
    }

    @Transactional
    public void cancelFromWarehouse(Long productId, Long warehouseId, Integer quantity) {
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
        inv.setQuantity(inv.getQuantity() + quantity);
        inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
        inventoryRepository.save(inv);
    }

    @Transactional
    public void deleteByProductId(Long productId) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tồn kho cho sản phẩm " + productId);
        }
        inventoryRepository.deleteAll(records);
    }

    @Transactional
    public void deleteByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
        inventoryRepository.delete(inv);
    }

    List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    private InventoryResponse toResponse(Inventory inv) {
        String warehouseName = null;
        if (inv.getWarehouseId() != null) {
            warehouseName = warehouseRepository.findById(inv.getWarehouseId())
                    .map(Warehouse::getName).orElse(null);
        }
        return new InventoryResponse(
                inv.getId(), inv.getProductId(), inv.getWarehouseId(),
                warehouseName, inv.getQuantity(), inv.getReservedQuantity(),
                inv.getAvailableQuantity()
        );
    }
}
