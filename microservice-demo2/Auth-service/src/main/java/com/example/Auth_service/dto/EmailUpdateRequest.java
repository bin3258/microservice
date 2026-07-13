package com.example.Auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailUpdateRequest {
    @NotBlank @Email
    private String email;

    public EmailUpdateRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
