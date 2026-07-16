package com.example.discount_service.dto;

public class ValidateRequest {

    private String code;
    private Long userId;
    private Double orderTotal;
    private Double shippingFee;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getOrderTotal() { return orderTotal; }
    public void setOrderTotal(Double orderTotal) { this.orderTotal = orderTotal; }
    public Double getShippingFee() { return shippingFee; }
    public void setShippingFee(Double shippingFee) { this.shippingFee = shippingFee; }
}
