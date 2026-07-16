package com.example.discount_service.dto;

public class MarkUsedRequest {

    private String code;
    private Long userId;
    private Long orderId;
    private Double discountAmount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
}
