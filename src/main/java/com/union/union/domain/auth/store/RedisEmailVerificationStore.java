package com.union.union.domain.auth.store;

import com.union.union.global.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationStore implements EmailVerificationStore {

    private final RedisService redisService;
    
    private static final String CODE_PREFIX = "email:auth:code:";
    private static final String VERIFIED_PREFIX = "email:auth:verified:";
    private static final long VERIFIED_EXPIRE_MINUTES = 10;

    @Override
    public void save(String email, String code, long expireTimeMillis) {
        long durationMillis = expireTimeMillis - System.currentTimeMillis();
        redisService.setValuesWithTimeout(CODE_PREFIX + email, code, Duration.ofMillis(durationMillis));
    }

    @Override
    public Optional<String> get(String email) {
        return redisService.get(CODE_PREFIX + email);
    }

    @Override
    public void delete(String email) {
        redisService.delete(CODE_PREFIX + email);
    }

    @Override
    public void markAsVerified(String email) {
        redisService.setValuesWithTimeout(VERIFIED_PREFIX + email, "true", Duration.ofMinutes(VERIFIED_EXPIRE_MINUTES));
    }

    @Override
    public boolean isVerified(String email) {
        return redisService.hasKey(VERIFIED_PREFIX + email);
    }
}
