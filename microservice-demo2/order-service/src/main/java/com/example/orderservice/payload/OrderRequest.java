package com.example.orderservice.payload;

import java.util.List;

public class OrderRequest {

	private Long userId;
	private String userName;
	private String userEmail;
	private String userPhone;
	private String shippingAddress;
	private String note;
	private Double shippingFee;
	private String city;
	private Double customerLat;
	private Double customerLng;
	private List<OrderItemRequest> items;

	public OrderRequest() {
	}

	public OrderRequest(Long userId, List<OrderItemRequest> items) {
		this.userId = userId;
		this.items = items;
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

	public Double getShippingFee() { return shippingFee; }
	public void setShippingFee(Double shippingFee) { this.shippingFee = shippingFee; }
	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }
	public Double getCustomerLat() { return customerLat; }
	public void setCustomerLat(Double customerLat) { this.customerLat = customerLat; }
	public Double getCustomerLng() { return customerLng; }
	public void setCustomerLng(Double customerLng) { this.customerLng = customerLng; }

	public List<OrderItemRequest> getItems() {
		return items;
	}

	public void setItems(List<OrderItemRequest> items) {
		this.items = items;
	}
}
