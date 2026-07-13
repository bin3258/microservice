package com.example.orderservice.payload;

public class OrderLineResponse {

	private Long productId;
	private ProductInfo product;
	private Integer quantity;
	private Double unitPrice;
	private Double lineTotal;
	private Long warehouseId;
	private String warehouseName;

	public OrderLineResponse() {
	}

	public OrderLineResponse(Long productId, ProductInfo product, Integer quantity, Double unitPrice, Double lineTotal) {
		this.productId = productId;
		this.product = product;
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

	public ProductInfo getProduct() {
		return product;
	}

	public void setProduct(ProductInfo product) {
		this.product = product;
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

	public Long getWarehouseId() { return warehouseId; }
	public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
	public String getWarehouseName() { return warehouseName; }
	public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
}
