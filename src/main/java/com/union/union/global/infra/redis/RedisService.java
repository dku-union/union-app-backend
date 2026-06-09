package com.union.union.global.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 (유효 시간 미지정)
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 데이터 저장 (유효 시간 지정)
    public void setValuesWithTimeout(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    // 데이터 조회
    public Optional<String> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((String) value);
    }

    // 데이터 삭제
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 키 존재 여부 확인
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ZSET: score 증가 (검색어 우선순위 누적)
    public void incrementScore(String key, String value, double delta) {
        redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    // ZSET: 상위 N개 추출 (인기 검색어 조회)
    public Set<Object> getTopRanks(String key, int limit) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, Math.max(0, limit - 1));
    }

    // 키 만료 시간 설정 (이미 존재하는 키의 TTL 갱신)
    public void setExpire(String key, Duration timeout) {
        redisTemplate.expire(key, timeout);
    }
}
