package com.example.Payment_service.dto;

import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Double amount;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String vnpTxnRef;
    private String vnpResponseCode;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {}

    public PaymentResponse(Long id, Long orderId, Double amount, String status, String paymentMethod, String transactionId, String vnpTxnRef, String vnpResponseCode, String vnpTransactionNo, String vnpBankCode, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.vnpTxnRef = vnpTxnRef;
        this.vnpResponseCode = vnpResponseCode;
        this.vnpTransactionNo = vnpTransactionNo;
        this.vnpBankCode = vnpBankCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getVnpTxnRef() { return vnpTxnRef; }
    public void setVnpTxnRef(String vnpTxnRef) { this.vnpTxnRef = vnpTxnRef; }
    public String getVnpResponseCode() { return vnpResponseCode; }
    public void setVnpResponseCode(String vnpResponseCode) { this.vnpResponseCode = vnpResponseCode; }
    public String getVnpTransactionNo() { return vnpTransactionNo; }
    public void setVnpTransactionNo(String vnpTransactionNo) { this.vnpTransactionNo = vnpTransactionNo; }
    public String getVnpBankCode() { return vnpBankCode; }
    public void setVnpBankCode(String vnpBankCode) { this.vnpBankCode = vnpBankCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
