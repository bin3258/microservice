package com.example.orderservice.service;

import com.example.orderservice.client.DiscountClient;
import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.messaging.OrderEventPublisher;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.payload.OrderItemRequest;
import com.example.orderservice.payload.OrderRequest;
import com.example.orderservice.payload.OrderResponse;
import com.example.orderservice.payload.OrderLineResponse;
import com.example.orderservice.payload.ProductInfo;
import com.example.orderservice.payload.UserInfo;
import com.example.orderservice.payload.WarehouseSelection;
import com.example.orderservice.repository.OrderRepository;
import com.example.shared.messaging.OrderCreatedEvent;
import com.example.shared.messaging.OrderStatusEvent;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orderRepository;
	private final ProductClient productClient;
	private final UserClient userClient;
	private final InventoryClient inventoryClient;
	private final OrderEventPublisher orderEventPublisher;
	private final SequenceGeneratorService sequenceGenerator;
	private final DiscountClient discountClient;

	public OrderService(OrderRepository orderRepository, ProductClient productClient, UserClient userClient, InventoryClient inventoryClient, OrderEventPublisher orderEventPublisher, SequenceGeneratorService sequenceGenerator, DiscountClient discountClient) {
		this.orderRepository = orderRepository;
		this.productClient = productClient;
		this.userClient = userClient;
		this.inventoryClient = inventoryClient;
		this.orderEventPublisher = orderEventPublisher;
		this.sequenceGenerator = sequenceGenerator;
		this.discountClient = discountClient;
	}

	public OrderResponse createOrder(OrderRequest request) {
		validateOrderRequest(request);
		UserInfo user;
		if (request.getUserName() != null && request.getUserEmail() != null) {
			user = new UserInfo(request.getUserId(), request.getUserName(), request.getUserEmail(), request.getUserPhone());
		} else {
			user = getUserOrThrow(request.getUserId());
		}
		Order order = new Order();
		order.setId(sequenceGenerator.generateSequence("order_seq"));
		order.setUserId(user.getId());
		order.setUserName(user.getName());
		order.setUserEmail(user.getEmail());
		order.setUserPhone(user.getPhone());

		int totalQuantity = 0;
		double totalPrice = 0.0;
		List<OrderCreatedEvent.OrderItemSnapshot> eventItems = new ArrayList<>();

		for (OrderItemRequest itemRequest : request.getItems()) {
			validateOrderItemRequest(itemRequest);
			ProductInfo product = getProductOrThrow(itemRequest.getProductId());
			double unitPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
			double lineTotal = unitPrice * itemRequest.getQuantity();

			OrderItem orderItem = new OrderItem();
			orderItem.setProductId(product.getId());
			orderItem.setProductName(product.getName());
			orderItem.setProductImg(product.getImg());
			orderItem.setRam(product.getRam());
			orderItem.setStorage(product.getStorage());
			orderItem.setScreenResolution(product.getScreenResolution());
			orderItem.setScreenTechnology(product.getScreenTechnology());
			orderItem.setBattery(product.getBattery());
			orderItem.setColor(product.getColor());
			orderItem.setQuantity(itemRequest.getQuantity());
			orderItem.setUnitPrice(unitPrice);
			orderItem.setLineTotal(lineTotal);
			orderItem.setWarehouseId(itemRequest.getWarehouseId());
			orderItem.setWarehouseName(itemRequest.getWarehouseName());
			order.addItem(orderItem);
			eventItems.add(new OrderCreatedEvent.OrderItemSnapshot(
					product.getId(),
					product.getName(),
					product.getImg(),
					product.getRam(),
					product.getStorage(),
					product.getScreenResolution(),
					product.getScreenTechnology(),
					product.getBattery(),
					product.getColor(),
					itemRequest.getQuantity(),
					unitPrice,
					lineTotal
			));

			totalQuantity += itemRequest.getQuantity();
			totalPrice += lineTotal;
		}

		double shippingFee = request.getShippingFee() != null ? request.getShippingFee() : 0;
		double productTotal = totalPrice;
		totalPrice += shippingFee;

		String discountCode = request.getDiscountCode();
		double discountAmount = 0.0;
		if (discountCode != null && !discountCode.isBlank()) {
			try {
				Map<String, Object> validateReq = new HashMap<>();
				validateReq.put("code", discountCode);
				validateReq.put("userId", request.getUserId());
				validateReq.put("orderTotal", totalPrice);
				validateReq.put("shippingFee", shippingFee);

				Map<String, Object> validateRes = discountClient.validate(validateReq);
				boolean valid = (boolean) validateRes.getOrDefault("valid", false);
				if (valid) {
					discountAmount = ((Number) validateRes.getOrDefault("discountAmount", 0.0)).doubleValue();
					String type = (String) validateRes.get("type");
					if ("SHIPPING".equals(type)) {
						shippingFee = Math.max(0, shippingFee - discountAmount);
					}
					totalPrice = Math.max(0, totalPrice - discountAmount);
				}
			} catch (Exception e) {
				log.warn("Failed to validate discount code {}: {}", discountCode, e.getMessage());
			}
		}

		order.setDiscountCode(discountAmount > 0 ? discountCode : null);
		order.setDiscountAmount(discountAmount > 0 ? discountAmount : null);
		order.setTotalQuantity(totalQuantity);
		order.setTotalPrice(totalPrice);
		order.setShippingFee(shippingFee);
		order.setCity(request.getCity());
		order.setCustomerLat(request.getCustomerLat());
		order.setCustomerLng(request.getCustomerLng());
		order.setStatus("PENDING");
		order.setPaymentMethod(request.getPaymentMethod());
		order.setShippingAddress(request.getShippingAddress());
		order.setNote(request.getNote());

		boolean isVnpay = "VNPAY".equals(request.getPaymentMethod());
		if (isVnpay) {
			order.setStatus("PENDING_PAYMENT");
		}

		Order savedOrder = orderRepository.save(order);

		for (OrderItem item : savedOrder.getItems()) {
			if (item.getWarehouseId() == null) continue;
			Map<String, Object> req = new HashMap<>();
			req.put("productId", item.getProductId());
			req.put("warehouseId", item.getWarehouseId());
			req.put("quantity", item.getQuantity());
			try {
				inventoryClient.reserveFromWarehouse(req);
			} catch (FeignException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Đặt hàng thất bại: không thể đặt trước tồn kho cho sản phẩm " + item.getProductId());
			}
		}

		if (discountAmount > 0) {
			try {
				Map<String, Object> markReq = new HashMap<>();
				markReq.put("code", discountCode);
				markReq.put("userId", request.getUserId());
				markReq.put("orderId", savedOrder.getId());
				markReq.put("discountAmount", discountAmount);
				discountClient.markUsed(markReq);
			} catch (Exception e) {
				log.warn("Failed to mark discount {} as used: {}", discountCode, e.getMessage());
			}
		}

		OrderResponse response = buildOrderResponse(savedOrder);
		if (!isVnpay) {
			orderEventPublisher.publishOrderCreated(new OrderCreatedEvent(
					savedOrder.getId(),
					user.getId(),
					user.getName(),
					user.getEmail(),
					user.getPhone(),
					eventItems,
					totalQuantity,
					totalPrice,
					savedOrder.getDiscountCode(),
					savedOrder.getDiscountAmount()
			));
		}
		return response;
	}

	public OrderResponse confirmPayment(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
		if (!"PENDING_PAYMENT".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn hàng không ở trạng thái chờ thanh toán");
		}
		order.setStatus("PAID");
		order = orderRepository.save(order);

		OrderCreatedEvent event = new OrderCreatedEvent(
				order.getId(),
				order.getUserId(),
				order.getUserName(),
				order.getUserEmail(),
				order.getUserPhone(),
				order.getItems().stream().map(item -> new OrderCreatedEvent.OrderItemSnapshot(
						item.getProductId(),
						item.getProductName(),
						item.getProductImg(),
						item.getRam(),
						item.getStorage(),
						item.getScreenResolution(),
						item.getScreenTechnology(),
						item.getBattery(),
						item.getColor(),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getLineTotal()
				)).toList(),
				order.getTotalQuantity(),
				order.getTotalPrice(),
				order.getDiscountCode(),
				order.getDiscountAmount()
		);
		orderEventPublisher.publishOrderCreated(event);
		return buildOrderResponse(order);
	}

	public List<OrderResponse> getAllOrders() {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		return orderRepository.findAll(sort).stream()
				.map(this::buildOrderResponse)
				.toList();
	}

	public List<OrderResponse> getOrdersByUserId(Long userId) {
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		return orderRepository.findByUserId(userId, sort).stream()
				.map(this::buildOrderResponse)
				.toList();
	}

	public boolean hasPurchasedProduct(Long userId, Long productId) {
		return !orderRepository.findPurchasedByUserAndProduct(userId, productId).isEmpty();
	}

	public OrderResponse getOrderById(Long id) {
		return orderRepository.findById(id)
				.map(this::buildOrderResponse)
				.orElse(null);
	}

	public void updateOrderStatus(Long id, String status, List<WarehouseSelection> warehouseSelections) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

		String previousStatus = order.getStatus();

		if ("CONFIRMED".equals(status)) {
			for (OrderItem item : order.getItems()) {
				if (item.getWarehouseId() == null) continue;
				Map<String, Object> req = new HashMap<>();
				req.put("productId", item.getProductId());
				req.put("warehouseId", item.getWarehouseId());
				req.put("quantity", item.getQuantity());
				try {
					inventoryClient.confirmFromWarehouse(req);
				} catch (FeignException e) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Xác nhận tồn kho thất bại cho sản phẩm " + item.getProductId() + ": " + e.getMessage());
				}
			}
		}

		if ("CANCELLED".equals(status)) {
			boolean wasPending = "PENDING".equals(previousStatus);
			for (OrderItem item : order.getItems()) {
				if (item.getWarehouseId() == null) continue;
				Map<String, Object> req = new HashMap<>();
				req.put("productId", item.getProductId());
				req.put("warehouseId", item.getWarehouseId());
				req.put("quantity", item.getQuantity());
				try {
					if (wasPending) {
						inventoryClient.releaseFromWarehouse(req);
					} else {
						inventoryClient.cancelFromWarehouse(req);
					}
				} catch (FeignException e) {
					// Log but don't block cancellation
				}
			}
			if (order.getDiscountCode() != null) {
				try {
					Map<String, Object> releaseReq = new HashMap<>();
					releaseReq.put("code", order.getDiscountCode());
					releaseReq.put("userId", order.getUserId());
					releaseReq.put("orderId", order.getId());
					releaseReq.put("discountAmount", order.getDiscountAmount());
					discountClient.release(releaseReq);
				} catch (Exception e) {
					log.warn("Failed to release discount {} for order {}: {}", order.getDiscountCode(), order.getId(), e.getMessage());
				}
			}
		}

		order.setStatus(status);
		orderRepository.save(order);

		publishStatusEvent(order);
	}

	public OrderResponse updateOrder(Long id, String shippingAddress, String note, List<OrderItemRequest> items) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
		if (!"PENDING".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể sửa đơn hàng khi đang chờ xác nhận");
		}
		if (shippingAddress != null) order.setShippingAddress(shippingAddress);
		if (note != null) order.setNote(note);
		if (items != null) {
			List<OrderItem> orderItems = new ArrayList<>();
			for (OrderItemRequest itemReq : items) {
				ProductInfo product = productClient.getProductById(itemReq.getProductId());
				double unitPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
				double lineTotal = unitPrice * itemReq.getQuantity();
				OrderItem oi = new OrderItem();
				oi.setId((long) orderItems.size() + 1);
				oi.setProductId(itemReq.getProductId());
				oi.setProductName(product.getName());
				oi.setProductImg(product.getImg());
				oi.setRam(product.getRam());
				oi.setStorage(product.getStorage());
				oi.setScreenResolution(product.getScreenResolution());
				oi.setScreenTechnology(product.getScreenTechnology());
				oi.setBattery(product.getBattery());
				oi.setColor(product.getColor());
				oi.setQuantity(itemReq.getQuantity());
				oi.setUnitPrice(unitPrice);
				oi.setLineTotal(lineTotal);
				orderItems.add(oi);
			}
			order.setItems(orderItems);
			int totalQuantity = orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
			double totalPrice = orderItems.stream().mapToDouble(OrderItem::getLineTotal).sum();
			order.setTotalQuantity(totalQuantity);
			order.setTotalPrice(totalPrice);
		}
		return buildOrderResponse(orderRepository.save(order));
	}

	public void cancelOrder(Long id, Long userId) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
		if (!"PENDING".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy đơn hàng khi đang chờ xác nhận");
		}
		if (!order.getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không thể hủy đơn hàng của người khác");
		}

		releaseInventoryAndDiscount(order);

		order.setStatus("CANCELLED");
		orderRepository.save(order);

		publishStatusEvent(order);
	}

	public void deleteOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
		if (!"PENDING".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa đơn hàng khi đang chờ xác nhận");
		}
		orderRepository.delete(order);
	}

	private void releaseInventoryAndDiscount(Order order) {
		for (OrderItem item : order.getItems()) {
			if (item.getWarehouseId() == null) continue;
			Map<String, Object> req = new HashMap<>();
			req.put("productId", item.getProductId());
			req.put("warehouseId", item.getWarehouseId());
			req.put("quantity", item.getQuantity());
			try {
				inventoryClient.releaseFromWarehouse(req);
			} catch (Exception e) {
				log.warn("Failed to release inventory for order {}: {}", order.getId(), e.getMessage());
			}
		}
		if (order.getDiscountCode() != null) {
			try {
				Map<String, Object> releaseReq = new HashMap<>();
				releaseReq.put("code", order.getDiscountCode());
				releaseReq.put("userId", order.getUserId());
				releaseReq.put("orderId", order.getId());
				releaseReq.put("discountAmount", order.getDiscountAmount());
				discountClient.release(releaseReq);
			} catch (Exception e) {
				log.warn("Failed to release discount {} for order {}: {}", order.getDiscountCode(), order.getId(), e.getMessage());
			}
		}
	}

	private void publishStatusEvent(Order order) {
		List<OrderStatusEvent.OrderItemSnapshot> eventItems = order.getItems().stream()
				.<OrderStatusEvent.OrderItemSnapshot>map(item -> new OrderStatusEvent.OrderItemSnapshot(
						item.getProductId(),
						item.getProductName(),
						item.getProductImg(),
						item.getRam(),
						item.getStorage(),
						item.getScreenResolution(),
						item.getScreenTechnology(),
						item.getBattery(),
						item.getColor(),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getLineTotal()
				))
				.toList();

		OrderStatusEvent event = new OrderStatusEvent();
		event.setOrderId(order.getId());
		event.setUserId(order.getUserId());
		event.setUserName(order.getUserName());
		event.setUserEmail(order.getUserEmail());
		event.setItems(eventItems);
		event.setTotalQuantity(order.getTotalQuantity());
		event.setTotalPrice(order.getTotalPrice());
		event.setStatus(order.getStatus());
		event.setShippingFee(order.getShippingFee());
		event.setDiscountCode(order.getDiscountCode());
		event.setDiscountAmount(order.getDiscountAmount());

		orderEventPublisher.publishOrderStatusEvent(event);
	}

	private OrderResponse buildOrderResponse(Order order) {
		UserInfo user = new UserInfo(order.getUserId(), order.getUserName(), order.getUserEmail(), order.getUserPhone());
		List<OrderLineResponse> items = order.getItems().stream()
				.map(this::buildOrderLineResponse)
				.toList();
		return new OrderResponse(
				order.getId(),
				user,
				items,
				order.getTotalQuantity(),
				order.getTotalPrice(),
				order.getShippingFee(),
				order.getCity(),
				order.getStatus(),
				order.getPaymentMethod(),
				order.getShippingAddress(),
				order.getNote(),
				order.getDiscountCode(),
				order.getDiscountAmount()
		);
	}

	private OrderLineResponse buildOrderLineResponse(OrderItem orderItem) {
		ProductInfo product = new ProductInfo(
				orderItem.getProductId(),
				orderItem.getProductName(),
				orderItem.getUnitPrice(),
				null,
				orderItem.getProductImg()
		);
		product.setRam(orderItem.getRam());
		product.setStorage(orderItem.getStorage());
		product.setScreenResolution(orderItem.getScreenResolution());
		product.setScreenTechnology(orderItem.getScreenTechnology());
		product.setBattery(orderItem.getBattery());
		product.setColor(orderItem.getColor());
		OrderLineResponse resp = new OrderLineResponse(
				orderItem.getProductId(),
				product,
				orderItem.getQuantity(),
				orderItem.getUnitPrice(),
				orderItem.getLineTotal()
		);
		resp.setWarehouseId(orderItem.getWarehouseId());
		resp.setWarehouseName(orderItem.getWarehouseName());
		return resp;
	}

	private void validateOrderRequest(OrderRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu đơn hàng không được để trống");
		}
		if (request.getUserId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp userId");
		}
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn hàng phải có ít nhất một sản phẩm");
		}
	}

	private void validateOrderItemRequest(OrderItemRequest itemRequest) {
		if (itemRequest.getProductId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp productId");
		}
		if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng phải lớn hơn 0");
		}
	}

	private ProductInfo getProductOrThrow(Long productId) {
		try {
			ProductInfo product = productClient.getProductById(productId);
			if (product == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + productId);
			}
			return product;
		} catch (FeignException.NotFound ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + productId, ex);
		} catch (FeignException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối dịch vụ sản phẩm", ex);
		}
	}

	private UserInfo getUserOrThrow(Long userId) {
		try {
			UserInfo user = userClient.getUserById(userId);
			if (user == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã " + userId);
			}
			return user;
		} catch (FeignException.NotFound ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã " + userId, ex);
		} catch (FeignException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối dịch vụ người dùng", ex);
		}
	}
}
