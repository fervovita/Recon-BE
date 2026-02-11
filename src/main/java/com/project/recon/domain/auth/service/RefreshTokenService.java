package com.project.recon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = "RT:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                PREFIX + userId,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(Long userId) {
        Object refreshToken = redisTemplate.opsForValue().get(PREFIX + userId);
        return refreshToken != null ? refreshToken.toString() : null;
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
