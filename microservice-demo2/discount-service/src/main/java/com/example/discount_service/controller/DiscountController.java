package com.example.discount_service.controller;

import com.example.discount_service.dto.*;
import com.example.discount_service.entity.Discount;
import com.example.discount_service.entity.DiscountUser;
import com.example.discount_service.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public List<DiscountResponse> getAll() {
        return discountService.getAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DiscountResponse getById(@PathVariable Long id) {
        return toResponse(discountService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DiscountResponse> create(@Valid @RequestBody DiscountRequest req) {
        Discount d = discountService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(d));
    }

    @PutMapping("/{id}")
    public DiscountResponse update(@PathVariable Long id, @Valid @RequestBody DiscountRequest req) {
        return toResponse(discountService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/users")
    public List<Map<String, Long>> getAssignedUsers(@PathVariable Long id) {
        return discountService.getAssignedUsers(id).stream()
                .map(u -> Map.<String, Long>of("id", u.getId(), "userId", u.getUserId()))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/users")
    public ResponseEntity<Void> assignUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        discountService.assignUsers(id, userIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/users")
    public ResponseEntity<Void> removeAllUsers(@PathVariable Long id) {
        discountService.removeAllUsers(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    public List<DiscountResponse> getAvailable(@RequestParam Long userId) {
        return discountService.getAvailableDiscounts(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping("/validate")
    public ValidateResponse validate(@RequestBody ValidateRequest req) {
        return discountService.validate(req);
    }

    @PostMapping("/mark-used")
    public ResponseEntity<Void> markUsed(@RequestBody MarkUsedRequest req) {
        discountService.markUsed(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestBody MarkUsedRequest req) {
        discountService.release(req);
        return ResponseEntity.ok().build();
    }

    private DiscountResponse toResponse(Discount d) {
        DiscountResponse r = new DiscountResponse();
        r.setId(d.getId());
        r.setCode(d.getCode());
        r.setType(d.getType());
        r.setDiscountValue(d.getDiscountValue());
        r.setMinOrderValue(d.getMinOrderValue());
        r.setUsageLimit(d.getUsageLimit());
        r.setUsedCount(d.getUsedCount());
        r.setStartDate(d.getStartDate() != null ? d.getStartDate().format(DTF) : null);
        r.setEndDate(d.getEndDate() != null ? d.getEndDate().format(DTF) : null);
        r.setIsActive(d.getIsActive());
        r.setDescription(d.getDescription());
        r.setCreatedAt(d.getCreatedAt() != null ? d.getCreatedAt().format(DTF) : null);
        r.setUpdatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt().format(DTF) : null);
        r.setAssignedToAll(discountService.getAssignedUsers(d.getId()).isEmpty());
        return r;
    }
}
