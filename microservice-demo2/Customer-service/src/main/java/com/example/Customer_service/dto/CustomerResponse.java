package com.example.Customer_service.dto;

public class CustomerResponse {
    private Long id;
    private Long authUserId;
    private String fullName;
    private String phone;
    private String email;
    private String token;

    public CustomerResponse() {}

    public CustomerResponse(Long id, Long authUserId, String fullName, String phone, String email, String token) {
        this.id = id;
        this.authUserId = authUserId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.token = token;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAuthUserId() { return authUserId; }
    public void setAuthUserId(Long authUserId) { this.authUserId = authUserId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
