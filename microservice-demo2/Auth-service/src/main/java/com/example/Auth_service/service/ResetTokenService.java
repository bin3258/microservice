package com.example.Auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ResetTokenService {

	private final StringRedisTemplate redisTemplate;
	private final long resetExpiration;

	private static final String RESET_PREFIX = "reset:";

	public ResetTokenService(StringRedisTemplate redisTemplate,
							 @Value("${jwt.reset-password-expiration}") long resetExpiration) {
		this.redisTemplate = redisTemplate;
		this.resetExpiration = resetExpiration;
	}

	public String createResetToken(Long userId) {
		String token = UUID.randomUUID().toString().replace("-", "");
		redisTemplate.opsForValue().set(RESET_PREFIX + token, userId.toString(),
				resetExpiration, TimeUnit.MILLISECONDS);
		return token;
	}

	public Long validateAndGetUserId(String token) {
		String userId = redisTemplate.opsForValue().get(RESET_PREFIX + token);
		if (userId == null) return null;
		return Long.parseLong(userId);
	}

	public void deleteResetToken(String token) {
		redisTemplate.delete(RESET_PREFIX + token);
	}
}
