package com.example.Auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {
    @NotBlank
    @Size(min = 6)
    private String password;

    public PasswordChangeRequest() {}

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
