package com.example.orderservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class Order {

	@Id
	private Long id;
	private Long userId;
	private String userName;
	private String userEmail;
	private String userPhone;
	private Integer totalQuantity;
	private Double totalPrice;
	private String status = "PENDING";
	private String shippingAddress;
	private String note;
	private Double shippingFee;
	private String city;
	private Double customerLat;
	private Double customerLng;

	private List<OrderItem> items = new ArrayList<>();

	public Order() {
	}

	public Order(Long id, Long userId, Integer totalQuantity, Double totalPrice) {
		this.id = id;
		this.userId = userId;
		this.totalQuantity = totalQuantity;
		this.totalPrice = totalPrice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items.clear();
		if (items != null) {
			items.forEach(this::addItem);
		}
	}

	public void addItem(OrderItem item) {
		items.add(item);
	}
}
