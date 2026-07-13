package com.example.Auth_service.dto;

public class ValidateResponse {
    private boolean valid;
    private String username;
    private String role;
    private Long userId;

    public ValidateResponse() {}

    public ValidateResponse(boolean valid, String username, String role, Long userId) {
        this.valid = valid;
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
