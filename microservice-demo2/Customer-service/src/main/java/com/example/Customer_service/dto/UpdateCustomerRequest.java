package com.example.Customer_service.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCustomerRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    private String email;

    public UpdateCustomerRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
