package com.example.Auth_service.service;

import com.example.Auth_service.dto.AuthResponse;
import com.example.Auth_service.dto.LoginRequest;
import com.example.Auth_service.dto.RefreshTokenRequest;
import com.example.Auth_service.dto.RegisterRequest;
import com.example.Auth_service.dto.ValidateResponse;
import com.example.Auth_service.entity.AuthUser;
import com.example.Auth_service.client.UserServiceClient;
import com.example.Auth_service.repository.AuthUserRepository;
import com.example.Auth_service.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserServiceClient userServiceClient;

    public AuthService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserServiceClient userServiceClient) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userServiceClient = userServiceClient;
    }

    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ten dang nhap da ton tai");
        }
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email da ton tai");
        }

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "CUSTOMER";
        if (!role.matches("ADMIN|MANAGER|CUSTOMER")) {
            role = "CUSTOMER";
        }

        String phone = request.getPhone();
        if (phone != null && !phone.isBlank() && authUserRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        AuthUser user = new AuthUser(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                phone,
                role
        );
        user = authUserRepository.save(user);

        try {
            if (phone == null || phone.isBlank()) {
                phone = "000000000" + user.getId();
            }
            String userName = request.getName();
            if (userName == null || userName.isBlank()) {
                userName = request.getUsername();
            }
            userServiceClient.createUser(Map.of(
                "id", user.getId(),
                "name", userName,
                "email", user.getEmail(),
                "phone", phone,
                "role", user.getRole()
            ));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể đồng bộ người dùng: " + e.getMessage());
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = null;
        try {
            refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getUsername(), user.getRole());
        } catch (Exception e) {
            System.out.println(">>> Redis unavailable, skipping refresh token: " + e.getMessage());
        }
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getEmail(), user.getRole(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tai khoan da bi vo hieu hoa");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = null;
        try {
            refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getUsername(), user.getRole());
        } catch (Exception e) {
            System.out.println(">>> Redis unavailable, skipping refresh token: " + e.getMessage());
        }
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getEmail(), user.getRole(), user.getId());
    }

    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        String[] data = refreshTokenService.validateAndGetData(request.getRefreshToken());
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token khong hop le hoac da het han");
        }

        Long userId = Long.parseLong(data[0]);
        String username = data[1];
        String role = data[2];

        refreshTokenService.deleteRefreshToken(request.getRefreshToken());

        String newAccessToken = jwtUtil.generateToken(userId, username, role);
        String newRefreshToken = null;
        try {
            newRefreshToken = refreshTokenService.createRefreshToken(userId, username, role);
        } catch (Exception e) {
            System.out.println(">>> Redis unavailable, skipping refresh token: " + e.getMessage());
        }
        return new AuthResponse(newAccessToken, newRefreshToken, username, null, role, userId);
    }

    public void logout(String accessToken) {
        Claims claims = jwtUtil.validateToken(accessToken);
        if (claims != null) {
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                refreshTokenService.blacklistAccessToken(accessToken, ttl);
            }
        }
    }

    public void changePassword(Long userId, String newPassword) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);
    }

    public void updateEmail(Long userId, String newEmail) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
        if (!user.getEmail().equals(newEmail) && authUserRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email da ton tai");
        }
        if (!user.getUsername().equals(newEmail) && authUserRepository.existsByUsername(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ten dang nhap da ton tai");
        }
        user.setEmail(newEmail);
        user.setUsername(newEmail);
        authUserRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!authUserRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung");
        }
        authUserRepository.deleteById(userId);
    }

    public void changeRole(Long userId, String role) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
        String newRole = role != null ? role.toUpperCase() : "CUSTOMER";
        if (!newRole.matches("ADMIN|MANAGER|CUSTOMER")) {
            newRole = "CUSTOMER";
        }
        user.setRole(newRole);
        authUserRepository.save(user);
    }

    public ValidateResponse validateToken(String token) {
        if (refreshTokenService.isBlacklisted(token)) {
            return new ValidateResponse(false, null, null, null);
        }
        Claims claims = jwtUtil.validateToken(token);
        if (claims == null) {
            return new ValidateResponse(false, null, null, null);
        }
        return new ValidateResponse(true,
                claims.getSubject(),
                claims.get("role", String.class),
                claims.get("userId", Long.class));
    }
}
