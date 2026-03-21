package com.union.union.domain.auth.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEmailVerificationStore implements EmailVerificationStore {

    private final Map<String, VerificationData> store = new ConcurrentHashMap<>();

    @Override
    public void save(String email, String code, long expireTimeMillis) {
        store.put(email, new VerificationData(code, expireTimeMillis));
    }

    @Override
    public Optional<String> get(String email) {
        VerificationData data = store.get(email);
        
        if (data == null) {
            return Optional.empty();
        }

        // 만료 시간 체크
        if (System.currentTimeMillis() > data.expireTimeMillis()) {
            store.remove(email);
            return Optional.empty();
        }

        return Optional.of(data.code());
    }

    @Override
    public void delete(String email) {
        store.remove(email);
    }

    /**
     * 내부 데이터 구조
     */
    private record VerificationData(String code, long expireTimeMillis) {}
}
