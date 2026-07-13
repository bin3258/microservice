package com.example.Inventory_service.controller;

import com.example.Inventory_service.dto.WarehouseDTO;
import com.example.Inventory_service.entity.Warehouse;
import com.example.Inventory_service.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    public List<WarehouseDTO> getAll() {
        return warehouseRepository.findAll().stream()
                .map(w -> new WarehouseDTO(w.getId(), w.getName(), w.getAddress(), w.getLatitude(), w.getLongitude()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getById(@PathVariable Long id) {
        return warehouseRepository.findById(id)
                .map(w -> ResponseEntity.ok(new WarehouseDTO(w.getId(), w.getName(), w.getAddress(), w.getLatitude(), w.getLongitude())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WarehouseDTO> create(@RequestBody WarehouseDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kho không được để trống");
        }
        Warehouse w = new Warehouse(dto.getName(), dto.getAddress());
        w.setLatitude(dto.getLatitude());
        w.setLongitude(dto.getLongitude());
        w = warehouseRepository.save(w);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WarehouseDTO(w.getId(), w.getName(), w.getAddress(), w.getLatitude(), w.getLongitude()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDTO> update(@PathVariable Long id, @RequestBody WarehouseDTO dto) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kho"));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            w.setName(dto.getName());
        }
        if (dto.getAddress() != null) {
            w.setAddress(dto.getAddress());
        }
        if (dto.getLatitude() != null) w.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) w.setLongitude(dto.getLongitude());
        w = warehouseRepository.save(w);
        return ResponseEntity.ok(new WarehouseDTO(w.getId(), w.getName(), w.getAddress(), w.getLatitude(), w.getLongitude()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kho");
        }
        warehouseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
