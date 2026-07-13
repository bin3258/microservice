package com.example.shared.messaging;

import java.util.List;

public class OrderCreatedEvent {

	private Long orderId;
	private Long userId;
	private String userName;
	private String userEmail;
	private String userPhone;
	private List<OrderItemSnapshot> items;
	private Integer totalQuantity;
	private Double totalPrice;

	public OrderCreatedEvent() {
	}

	public OrderCreatedEvent(Long orderId, Long userId, String userName, String userEmail, String userPhone, List<OrderItemSnapshot> items, Integer totalQuantity, Double totalPrice) {
		this.orderId = orderId;
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.userPhone = userPhone;
		this.items = items;
		this.totalQuantity = totalQuantity;
		this.totalPrice = totalPrice;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public List<OrderItemSnapshot> getItems() {
		return items;
	}

	public void setItems(List<OrderItemSnapshot> items) {
		this.items = items;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public static class OrderItemSnapshot {

		private Long productId;
		private String productName;
		private Integer quantity;
		private Double unitPrice;
		private Double lineTotal;

		public OrderItemSnapshot() {
		}

		public OrderItemSnapshot(Long productId, String productName, Integer quantity, Double unitPrice, Double lineTotal) {
			this.productId = productId;
			this.productName = productName;
			this.quantity = quantity;
			this.unitPrice = unitPrice;
			this.lineTotal = lineTotal;
		}

		public Long getProductId() {
			return productId;
		}

		public void setProductId(Long productId) {
			this.productId = productId;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}

		public Double getUnitPrice() {
			return unitPrice;
		}

		public void setUnitPrice(Double unitPrice) {
			this.unitPrice = unitPrice;
		}

		public Double getLineTotal() {
			return lineTotal;
		}

		public void setLineTotal(Double lineTotal) {
			this.lineTotal = lineTotal;
		}
	}
}
