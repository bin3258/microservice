package com.example.notificationservice.controller;

import com.example.notificationservice.dto.PasswordResetRequest;
import com.example.notificationservice.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

	private final EmailService emailService;

	public NotificationController(EmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping("/send-password-reset")
	public ResponseEntity<Void> sendPasswordReset(@RequestBody PasswordResetRequest request) {
		emailService.sendPasswordReset(request);
		return ResponseEntity.ok().build();
	}
}
