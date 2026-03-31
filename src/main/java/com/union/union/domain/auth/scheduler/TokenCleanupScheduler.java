package com.union.union.domain.auth.scheduler;

import com.union.union.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "${token-cleanup.refresh-token-cron}")
    public void cleanupExpiredAndRevokedRefreshTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredAndRevoked();
        log.info("Refresh token cleanup completed. deletedCount={}", deletedCount);
    }
}
