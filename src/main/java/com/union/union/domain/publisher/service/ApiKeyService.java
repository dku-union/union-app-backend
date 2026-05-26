package com.union.union.domain.publisher.service;

import com.union.union.domain.publisher.entity.ApiKey;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.repository.ApiKeyRepository;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyService {

    public static final String SCOPE_NOTIFICATIONS_SEND = "notifications:send";
    private static final String KEY_PREFIX = "uk_live_";
    private static final int RAW_BODY_LENGTH = 32;
    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final PublisherRepository publisherRepository;

    public IssueResult issue(UUID publisherId, String name) {
        Publisher publisher = publisherRepository.findByPublisherId(publisherId)
                .orElseThrow(() -> new EntityNotFoundException("Publisher를 찾을 수 없습니다"));

        String rawBody = randomBase62(RAW_BODY_LENGTH);
        String rawKey = KEY_PREFIX + rawBody;
        String keyHash = sha256(rawKey);
        String keyPrefix = rawKey.substring(0, 12);

        ApiKey saved = apiKeyRepository.save(ApiKey.builder()
                .publisher(publisher)
                .keyPrefix(keyPrefix)
                .keyHash(keyHash)
                .name(name)
                .scopes(SCOPE_NOTIFICATIONS_SEND)
                .build());

        log.info("API Key 발급. publisherId={}, apiKeyId={}, name={}", publisherId, saved.getId(), name);
        return new IssueResult(
                saved.getId(),
                rawKey,
                keyPrefix,
                saved.getName(),
                saved.getScopes(),
                saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ApiKey> list(UUID publisherId) {
        return apiKeyRepository.findByPublisher_PublisherIdOrderByCreatedAtDesc(publisherId);
    }

    public void revoke(UUID publisherId, Long apiKeyId) {
        ApiKey key = apiKeyRepository.findByIdAndPublisher_PublisherId(apiKeyId, publisherId)
                .orElseThrow(() -> new EntityNotFoundException("API Key를 찾을 수 없습니다"));
        if (!key.isRevoked()) {
            key.revoke();
            log.info("API Key 폐기. publisherId={}, apiKeyId={}", publisherId, apiKeyId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ApiKey> verify(String rawKey) {
        if (rawKey == null || !rawKey.startsWith(KEY_PREFIX)) return Optional.empty();
        String hash = sha256(rawKey);
        return apiKeyRepository.findByKeyHash(hash).filter(k -> !k.isRevoked());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void touchAsync(Long apiKeyId, String ip) {
        apiKeyRepository.findById(apiKeyId).ifPresent(k -> k.touch(ip));
    }

    private static String randomBase62(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 미지원", e);
        }
    }

    public record IssueResult(
            Long id,
            String rawKey,
            String keyPrefix,
            String name,
            String scopes,
            java.time.LocalDateTime createdAt
    ) {}
}
