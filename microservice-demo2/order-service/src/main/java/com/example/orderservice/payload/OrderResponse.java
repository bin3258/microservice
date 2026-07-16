package com.example.orderservice.payload;

import java.util.List;

public class OrderResponse {

	private Long orderId;
	private UserInfo user;
	private List<OrderLineResponse> items;
	private Integer totalQuantity;
	private Double totalPrice;
	private Double shippingFee;
	private String city;
	private String status;
	private String paymentMethod;
	private String shippingAddress;
	private String note;
	private String discountCode;
	private Double discountAmount;

	public OrderResponse() {
	}

	public OrderResponse(Long orderId, UserInfo user, List<OrderLineResponse> items, Integer totalQuantity, Double totalPrice, Double shippingFee, String city, String status, String paymentMethod, String shippingAddress, String note, String discountCode, Double discountAmount) {
		this.orderId = orderId;
		this.user = user;
		this.items = items;
		this.totalQuantity = totalQuantity;
		this.totalPrice = totalPrice;
		this.shippingFee = shippingFee;
		this.city = city;
		this.status = status;
		this.paymentMethod = paymentMethod;
		this.shippingAddress = shippingAddress;
		this.note = note;
		this.discountCode = discountCode;
		this.discountAmount = discountAmount;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	public List<OrderLineResponse> getItems() {
		return items;
	}

	public void setItems(List<OrderLineResponse> items) {
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

	public Double getShippingFee() { return shippingFee; }
	public void setShippingFee(Double shippingFee) { this.shippingFee = shippingFee; }

	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getDiscountCode() { return discountCode; }
	public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
	public Double getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
}
