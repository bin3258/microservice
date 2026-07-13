package com.example.Auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpiration;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public RefreshTokenService(StringRedisTemplate redisTemplate,
                               @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiration = refreshExpiration;
    }

    public String createRefreshToken(Long userId, String username, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String data = userId + ":" + username + ":" + role;
        redisTemplate.opsForValue().set(REFRESH_PREFIX + token, data, refreshExpiration, TimeUnit.MILLISECONDS);
        return token;
    }

    public String[] validateAndGetData(String refreshToken) {
        String data = redisTemplate.opsForValue().get(REFRESH_PREFIX + refreshToken);
        if (data == null) return null;
        return data.split(":", 3);
    }

    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(REFRESH_PREFIX + refreshToken);
    }

    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "blacklisted",
                ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
