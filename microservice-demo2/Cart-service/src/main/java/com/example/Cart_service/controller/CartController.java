package com.example.Cart_service.controller;

import com.example.Cart_service.dto.CartRequest;
import com.example.Cart_service.dto.CartResponse;
import com.example.Cart_service.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public List<CartResponse> getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long userId, @RequestBody CartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long itemId, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(cartService.updateQuantity(itemId, body.get("quantity")));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // deprecated – kept for backward compatibility
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantityByProduct(@PathVariable Long userId, @PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(cartService.updateQuantityByProduct(userId, productId, body.get("quantity")));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItemByProduct(@PathVariable Long userId, @PathVariable Long productId) {
        cartService.removeItemByProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
