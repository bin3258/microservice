package com.example.Customer_service.client;

import com.example.Customer_service.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "Auth-service", path = "/api/auth")
public interface AuthClient {
    @PostMapping("/register")
    AuthResponse register(@RequestBody Map<String, Object> request);

    @PutMapping("/{userId}/email")
    void updateEmail(@PathVariable("userId") Long userId, @RequestBody Map<String, String> body);
}
