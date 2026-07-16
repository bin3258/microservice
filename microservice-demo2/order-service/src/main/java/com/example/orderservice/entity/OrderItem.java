package com.example.orderservice.entity;

public class OrderItem {

	private Long id;
	private Long productId;
	private String productName;
	private String productImg;
	private String ram;
	private String storage;
	private String screenResolution;
	private String screenTechnology;
	private String battery;
	private String color;
	private Integer quantity;
	private Double unitPrice;
	private Double lineTotal;
	private Long warehouseId;
	private String warehouseName;

	public OrderItem() {
	}

	public OrderItem(Long id, Long productId, Integer quantity, Double unitPrice, Double lineTotal) {
		this.id = id;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getProductId() { return productId; }
	public void setProductId(Long productId) { this.productId = productId; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getProductImg() { return productImg; }
	public void setProductImg(String productImg) { this.productImg = productImg; }
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
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public Double getUnitPrice() { return unitPrice; }
	public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
	public Double getLineTotal() { return lineTotal; }
	public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
	public Long getWarehouseId() { return warehouseId; }
	public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
	public String getWarehouseName() { return warehouseName; }
	public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
}
