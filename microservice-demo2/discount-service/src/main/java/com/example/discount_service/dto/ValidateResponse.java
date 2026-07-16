package com.example.discount_service.dto;

public class ValidateResponse {

    private boolean valid;
    private String type;
    private Double discountAmount;
    private String message;
    private Long discountId;

    public ValidateResponse() {}

    public ValidateResponse(boolean valid, String type, Double discountAmount, String message, Long discountId) {
        this.valid = valid;
        this.type = type;
        this.discountAmount = discountAmount;
        this.message = message;
        this.discountId = discountId;
    }

    public static ValidateResponse success(String type, Double discountAmount, Long discountId) {
        return new ValidateResponse(true, type, discountAmount, "Áp dụng mã giảm giá thành công", discountId);
    }

    public static ValidateResponse error(String message) {
        return new ValidateResponse(false, null, 0.0, message, null);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getDiscountId() { return discountId; }
    public void setDiscountId(Long discountId) { this.discountId = discountId; }
}
