package com.union.union.domain.auth.scheduler;

import com.union.union.domain.auth.repository.RefreshTokenRepository;
import com.union.union.domain.publisher.auth.repository.PublisherRefreshTokenRepository;
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
    private final PublisherRefreshTokenRepository publisherRefreshTokenRepository;

    @Transactional
    @Scheduled(cron = "${token-cleanup.refresh-token-cron}")
    public void cleanupExpiredAndRevokedRefreshTokens() {
        int userDeleted = refreshTokenRepository.deleteExpiredAndRevoked();
        int publisherDeleted = publisherRefreshTokenRepository.deleteExpiredAndRevoked();
        log.info("Refresh token cleanup completed. user={}, publisher={}", userDeleted, publisherDeleted);
    }
}
