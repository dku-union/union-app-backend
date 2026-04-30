package com.union.union.domain.publisher.auth.entity;

import com.union.union.domain.publisher.entity.Publisher;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * iOS 퍼블리셔 로그인 전용 refresh token.
 *
 * <p>{@code refresh_tokens}(User용)와 분리한 이유:
 * <ul>
 *     <li>FK가 다르다(users vs publishers)</li>
 *     <li>탈취 감지·일괄 무효화 정책을 사용자 세션에 영향 없이 운영하기 위함</li>
 *     <li>대시보드(NextAuth) 세션은 별도 단명 internal-jwt를 사용 → 본 테이블은 iOS 단독</li>
 * </ul>
 */
@Entity
@Table(name = "publisher_refresh_tokens", indexes = {
        @Index(name = "idx_publisher_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_publisher_refresh_token_publisher", columnList = "publisher_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublisherRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Builder
    public PublisherRefreshToken(String token, Publisher publisher, LocalDateTime expiresAt) {
        this.token = token;
        this.publisher = publisher;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public UUID getPublisherUuid() {
        return publisher.getPublisherId();
    }
}
