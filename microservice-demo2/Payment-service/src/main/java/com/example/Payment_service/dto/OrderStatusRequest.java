package com.example.Payment_service.dto;

public class OrderStatusRequest {
    private Long orderId;
    private String status;

    public OrderStatusRequest() {}

    public OrderStatusRequest(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
