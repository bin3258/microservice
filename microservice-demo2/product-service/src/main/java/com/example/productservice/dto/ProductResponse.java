package com.example.productservice.dto;

public class ProductResponse {

	private Long id;
	private String name;
	private Double price;
	private String img;
	private Long categoryId;
	private String categoryName;
	private String description;
	private String ram;
	private String storage;
	private String screenResolution;
	private String screenTechnology;
	private String battery;
	private String color;
	private Double salePrice;
	private boolean deleted;
	private Integer quantity;
	private Integer reservedQuantity;
	private Integer availableQuantity;

	public ProductResponse() {
	}

	public ProductResponse(Long id, String name, Double price, String img, Long categoryId, String categoryName, boolean deleted) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.img = img;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.deleted = deleted;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getRam() { return ram; }
	public void setRam(String ram) { this.ram = ram; }
	public String getStorage() { return storage; }
	public void setStorage(String storage) { this.storage = storage; }
	public String getScreenResolution() { return screenResolution; }
	public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
	public String getScreenTechnology() { return screenTechnology; }
	public void setScreenTechnology(String screenTechnology) { this.screenTechnology = screenTechnology; }
	public String getBattery() { return battery; }
	public void setBattery(String battery) { this.battery = battery; }
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	public Double getSalePrice() { return salePrice; }
	public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getReservedQuantity() {
		return reservedQuantity;
	}

	public void setReservedQuantity(Integer reservedQuantity) {
		this.reservedQuantity = reservedQuantity;
	}

	public Integer getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(Integer availableQuantity) {
		this.availableQuantity = availableQuantity;
	}
}
