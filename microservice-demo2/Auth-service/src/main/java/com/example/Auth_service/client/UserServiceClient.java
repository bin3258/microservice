package com.example.Auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/api/users")
    Map<String, Object> createUser(@RequestBody Map<String, Object> request);
}
