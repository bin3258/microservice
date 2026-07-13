package com.example.shared.messaging;

public class PaymentCompletedEvent {
    private Long paymentId;
    private Long orderId;
    private String status;
    private Double amount;
    private String transactionId;

    public PaymentCompletedEvent() {}

    public PaymentCompletedEvent(Long paymentId, Long orderId, String status, Double amount, String transactionId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
