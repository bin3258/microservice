package com.example.orderservice.payload;

public class OrderItemRequest {

	private Long productId;
	private Integer quantity;
	private Long warehouseId;
	private String warehouseName;

	public OrderItemRequest() {
	}

	public OrderItemRequest(Long productId, Integer quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public Long getProductId() { return productId; }
	public void setProductId(Long productId) { this.productId = productId; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public Long getWarehouseId() { return warehouseId; }
	public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
	public String getWarehouseName() { return warehouseName; }
	public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
}
