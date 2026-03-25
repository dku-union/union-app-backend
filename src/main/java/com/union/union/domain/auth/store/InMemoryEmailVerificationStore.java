package com.union.union.domain.auth.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEmailVerificationStore implements EmailVerificationStore {

    private final Map<String, VerifiedStatus> verifiedStore = new ConcurrentHashMap<>();
    private static final long VERIFIED_EXPIRE_TIME = 10 * 60 * 1000L; // 인증 완료 상태 유지 시간 (10분)

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

    @Override
    public void markAsVerified(String email) {
        verifiedStore.put(email, new VerifiedStatus(System.currentTimeMillis() + VERIFIED_EXPIRE_TIME));
    }

    @Override
    public boolean isVerified(String email) {
        VerifiedStatus status = verifiedStore.get(email);
        if (status == null) return false;

        if (System.currentTimeMillis() > status.expireTime()) {
            verifiedStore.remove(email);
            return false;
        }
        return true;
    }

    /**
     * 내부 데이터 구조
     */
    private record VerificationData(String code, long expireTimeMillis) {}
    private record VerifiedStatus(long expireTime) {}
}
