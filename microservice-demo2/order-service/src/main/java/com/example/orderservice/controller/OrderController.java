package com.example.orderservice.controller;

import com.example.orderservice.payload.OrderItemRequest;
import com.example.orderservice.payload.OrderRequest;
import com.example.orderservice.payload.OrderResponse;
import com.example.orderservice.payload.WarehouseSelection;
import com.example.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<OrderResponse> getAllOrders(HttpServletRequest request) {
		requireAdmin(request);
		return orderService.getAllOrders();
	}

	@GetMapping("/user/{userId}")
	public List<OrderResponse> getOrdersByUserId(HttpServletRequest request, @PathVariable Long userId) {
		Long currentUserId = getCurrentUserId(request);
		String role = getCurrentRole(request);
		if (!isAdminOrManager(role) && !currentUserId.equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khong the xem don hang cua nguoi khac");
		}
		return orderService.getOrdersByUserId(userId);
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getOrderById(HttpServletRequest request, @PathVariable Long id) {
		OrderResponse response = orderService.getOrderById(id);
		if (response == null) {
			return ResponseEntity.notFound().build();
		}
		Long currentUserId = getCurrentUserId(request);
		String role = getCurrentRole(request);
		if (!isAdminOrManager(role) && !currentUserId.equals(response.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khong the xem don hang cua nguoi khac");
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest body) {
		OrderResponse response = orderService.createOrder(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/test")
	public ResponseEntity<Map<String, String>> testPost() {
		Map<String, String> m = new HashMap<>();
		m.put("message", "POST works");
		return ResponseEntity.ok(m);
	}

	@SuppressWarnings("unchecked")
	@PutMapping("/{id}")
	public ResponseEntity<OrderResponse> updateOrder(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
		Long currentUserId = getCurrentUserId(request);
		String role = getCurrentRole(request);
		List<OrderItemRequest> items = null;
		if (body.containsKey("items") && isAdminOrManager(role)) {
			List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) body.get("items");
			if (itemsRaw != null) {
				items = itemsRaw.stream().map(m -> {
					OrderItemRequest req = new OrderItemRequest();
					req.setProductId(m.get("productId") != null ? ((Number) m.get("productId")).longValue() : null);
					req.setQuantity(m.get("quantity") != null ? ((Number) m.get("quantity")).intValue() : null);
					return req;
				}).toList();
			}
		}
		OrderResponse response = orderService.updateOrder(id, (String) body.get("shippingAddress"), (String) body.get("note"), items);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/confirm-payment")
	public ResponseEntity<OrderResponse> confirmPayment(@PathVariable Long id) {
		OrderResponse response = orderService.confirmPayment(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/cancel")
	public ResponseEntity<Void> cancelOrder(HttpServletRequest request, @PathVariable Long id) {
		Long currentUserId = getCurrentUserId(request);
		orderService.cancelOrder(id, currentUserId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/user/{userId}/has-purchased/{productId}")
	public ResponseEntity<Map<String, Boolean>> hasPurchased(@PathVariable Long userId, @PathVariable Long productId) {
		boolean purchased = orderService.hasPurchasedProduct(userId, productId);
		return ResponseEntity.ok(Map.of("purchased", purchased));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(HttpServletRequest request, @PathVariable Long id) {
		requireAdmin(request);
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}

	@SuppressWarnings("unchecked")
	@PutMapping("/{id}/status")
	public ResponseEntity<Void> updateOrderStatus(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody Map<String, Object> body) {
		requireAdmin(servletRequest);
		String status = (String) body.get("status");
		List<Map<String, Object>> warehouseSelectionsRaw = (List<Map<String, Object>>) body.get("warehouseSelections");
		List<WarehouseSelection> warehouseSelections = null;
		if (warehouseSelectionsRaw != null) {
			warehouseSelections = warehouseSelectionsRaw.stream().map(m -> {
				WarehouseSelection ws = new WarehouseSelection();
				ws.setProductId(m.get("productId") != null ? ((Number) m.get("productId")).longValue() : null);
				ws.setWarehouseId(m.get("warehouseId") != null ? ((Number) m.get("warehouseId")).longValue() : null);
				ws.setWarehouseName(m.get("warehouseName") != null ? (String) m.get("warehouseName") : null);
				ws.setQuantity(m.get("quantity") != null ? ((Number) m.get("quantity")).intValue() : null);
				return ws;
			}).toList();
		}
		orderService.updateOrderStatus(id, status, warehouseSelections);
		return ResponseEntity.ok().build();
	}

	private boolean isAdminOrManager(String role) {
		return "ADMIN".equals(role) || "MANAGER".equals(role);
	}

	private void requireAdmin(HttpServletRequest request) {
		String role = (String) request.getAttribute("X-User-Role");
		if (!isAdminOrManager(role)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi ADMIN/MANAGER moi co quyen thuc hien thao tac nay");
		}
	}

	private Long getCurrentUserId(HttpServletRequest request) {
		Object attr = request.getAttribute("X-User-Id");
		if (attr instanceof Long) return (Long) attr;
		if (attr instanceof String) return Long.parseLong((String) attr);
		return null;
	}

	private String getCurrentRole(HttpServletRequest request) {
		return (String) request.getAttribute("X-User-Role");
	}
}
