package com.union.union.global.security.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "rate_limit:";

    /**
     * Sliding window counter 방식의 rate limiting.
     * @return true면 요청 허용, false면 차단
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = KEY_PREFIX + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == null) {
            return false;
        }
        if (count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }
        return count <= maxRequests;
    }

    /**
     * IP + endpoint 조합으로 rate limit 키 생성
     */
    public static String keyOf(String ip, String endpoint) {
        return endpoint + ":" + ip;
    }
}
