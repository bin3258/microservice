package com.example.Auth_service.controller;

import com.example.Auth_service.dto.*;
import com.example.Auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long userId, @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(userId, request.getPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long userId, @RequestParam String role) {
        authService.changeRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/email")
    public ResponseEntity<Void> updateEmail(@PathVariable Long userId, @Valid @RequestBody EmailUpdateRequest request) {
        authService.updateEmail(userId, request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(new ValidateResponse(false, null, null, null));
        }
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
