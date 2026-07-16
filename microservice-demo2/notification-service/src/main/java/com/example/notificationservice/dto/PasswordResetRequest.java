package com.example.notificationservice.dto;

public class PasswordResetRequest {

	private String email;
	private String token;
	private String resetUrl;
	private String userName;

	public PasswordResetRequest() {
	}

	public PasswordResetRequest(String email, String token, String resetUrl, String userName) {
		this.email = email;
		this.token = token;
		this.resetUrl = resetUrl;
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getResetUrl() {
		return resetUrl;
	}

	public void setResetUrl(String resetUrl) {
		this.resetUrl = resetUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
