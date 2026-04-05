package com.union.union.domain.analytics.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Analytics 전용 해시 유틸리티.
 *
 * <p>iOS 네이티브가 {@code SHA-256(userId + ":" + appId)} 로 생성한 hashedUserId 를
 * 서버에서 재계산하여 검증한다.
 *
 * <p>원본 userId 는 절대 DB에 저장되지 않는다.
 */
@Component
public class AnalyticsHasher {

    /**
     * SHA-256(userId.toString() + ":" + appId) 를 64자 hex 문자열로 반환.
     *
     * @param userId 현재 인증된 사용자 UUID (JWT sub 클레임)
     * @param appId  미니앱 식별자 (e.g. "com.union.soccer")
     * @return 64자 소문자 hex 해시
     */
    public String hash(UUID userId, String appId) {
        String input = userId.toString() + ":" + appId;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 은 Java SE 필수 알고리즘이므로 절대 발생하지 않음
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 네이티브가 전달한 hashedUserId 가 서버 재계산 값과 일치하는지 검증.
     *
     * @return true = 유효 / false = 위변조 의심
     */
    public boolean verify(UUID userId, String appId, String providedHash) {
        if (providedHash == null || providedHash.isBlank()) return false;
        return hash(userId, appId).equals(providedHash);
    }
}
