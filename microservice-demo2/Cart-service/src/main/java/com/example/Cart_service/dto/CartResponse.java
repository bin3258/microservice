package com.example.Cart_service.dto;

public class CartResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImg;
    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;
    private Long userId;

    public CartResponse() {}

    public CartResponse(Long id, Long productId, String productName, String productImg, Integer quantity, Double unitPrice, Double lineTotal, Long userId) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImg = productImg;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImg() { return productImg; }
    public void setProductImg(String productImg) { this.productImg = productImg; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
