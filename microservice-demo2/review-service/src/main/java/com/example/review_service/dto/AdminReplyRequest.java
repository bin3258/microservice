package com.example.review_service.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminReplyRequest {

    @NotBlank
    private String adminReply;

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
}
