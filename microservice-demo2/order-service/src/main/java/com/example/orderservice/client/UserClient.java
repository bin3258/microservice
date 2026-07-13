package com.example.orderservice.client;

import com.example.orderservice.payload.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

	@GetMapping("/api/users/{id}")
	UserInfo getUserById(@PathVariable("id") Long id);
}
