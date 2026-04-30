package com.union.union.domain.publisher.auth.store;

import com.union.union.global.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class RedisPublisherEmailVerificationStore implements PublisherEmailVerificationStore {

    private static final String CODE_PREFIX = "publisher:auth:code:";

    private final RedisService redisService;

    @Override
    public void save(String email, String code, long expireMillisFromEpoch) {
        long durationMillis = expireMillisFromEpoch - System.currentTimeMillis();
        if (durationMillis <= 0) return;
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
}
