package com.union.union.domain.publisher.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "publisher_api_keys",
        uniqueConstraints = @UniqueConstraint(name = "uk_publisher_api_keys_key_hash", columnNames = "key_hash"),
        indexes = {
                @Index(name = "idx_publisher_api_keys_publisher", columnList = "publisher_id"),
                @Index(name = "idx_publisher_api_keys_revoked", columnList = "revoked_at")
        }
)
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(name = "key_prefix", nullable = false, length = 16)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String scopes;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Builder
    public ApiKey(Publisher publisher, String keyPrefix, String keyHash, String name, String scopes) {
        this.publisher = publisher;
        this.keyPrefix = keyPrefix;
        this.keyHash = keyHash;
        this.name = name;
        this.scopes = scopes != null ? scopes : "notifications:send";
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public void touch(String ip) {
        this.lastUsedAt = LocalDateTime.now();
        this.lastUsedIp = ip;
    }

    public boolean hasScope(String scope) {
        if (scopes == null || scope == null) return false;
        for (String s : scopes.split(",")) {
            if (s.trim().equals(scope)) return true;
        }
        return false;
    }
}
