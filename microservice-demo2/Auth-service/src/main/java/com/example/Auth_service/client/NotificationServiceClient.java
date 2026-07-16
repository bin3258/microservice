package com.example.Auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

	@PostMapping("/api/notification/send-password-reset")
	void sendPasswordReset(@RequestBody Map<String, Object> request);
}
