package com.example.orderservice.service;

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
import feign.FeignException;
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

	private final OrderRepository orderRepository;
	private final ProductClient productClient;
	private final UserClient userClient;
	private final InventoryClient inventoryClient;
	private final OrderEventPublisher orderEventPublisher;
	private final SequenceGeneratorService sequenceGenerator;

	public OrderService(OrderRepository orderRepository, ProductClient productClient, UserClient userClient, InventoryClient inventoryClient, OrderEventPublisher orderEventPublisher, SequenceGeneratorService sequenceGenerator) {
		this.orderRepository = orderRepository;
		this.productClient = productClient;
		this.userClient = userClient;
		this.inventoryClient = inventoryClient;
		this.orderEventPublisher = orderEventPublisher;
		this.sequenceGenerator = sequenceGenerator;
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
			orderItem.setQuantity(itemRequest.getQuantity());
			orderItem.setUnitPrice(unitPrice);
			orderItem.setLineTotal(lineTotal);
			orderItem.setWarehouseId(itemRequest.getWarehouseId());
			orderItem.setWarehouseName(itemRequest.getWarehouseName());
			order.addItem(orderItem);
			eventItems.add(new OrderCreatedEvent.OrderItemSnapshot(
					product.getId(),
					product.getName(),
					itemRequest.getQuantity(),
					unitPrice,
					lineTotal
			));

			totalQuantity += itemRequest.getQuantity();
			totalPrice += lineTotal;
		}

		double shippingFee = request.getShippingFee() != null ? request.getShippingFee() : 0;
		totalPrice += shippingFee;

		order.setTotalQuantity(totalQuantity);
		order.setTotalPrice(totalPrice);
		order.setShippingFee(shippingFee);
		order.setCity(request.getCity());
		order.setCustomerLat(request.getCustomerLat());
		order.setCustomerLng(request.getCustomerLng());
		order.setStatus("PENDING");
		order.setShippingAddress(request.getShippingAddress());
		order.setNote(request.getNote());

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

		OrderResponse response = buildOrderResponse(savedOrder);
		orderEventPublisher.publishOrderCreated(new OrderCreatedEvent(
				savedOrder.getId(),
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				eventItems,
				totalQuantity,
				totalPrice
		));
		return response;
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
		}

		order.setStatus(status);
		orderRepository.save(order);
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
		order.setStatus("CANCELLED");
		orderRepository.save(order);
	}

	public void deleteOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
		if (!"PENDING".equals(order.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa đơn hàng khi đang chờ xác nhận");
		}
		orderRepository.delete(order);
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
				order.getShippingAddress(),
				order.getNote()
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
