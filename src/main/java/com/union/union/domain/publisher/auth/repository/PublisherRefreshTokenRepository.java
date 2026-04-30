package com.union.union.domain.publisher.auth.repository;

import com.union.union.domain.publisher.auth.entity.PublisherRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PublisherRefreshTokenRepository extends JpaRepository<PublisherRefreshToken, Long> {

    Optional<PublisherRefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE PublisherRefreshToken rt SET rt.revoked = true " +
            "WHERE rt.publisher.publisherId = :publisherId AND rt.revoked = false")
    void revokeAllByPublisherId(@Param("publisherId") UUID publisherId);

    @Modifying
    @Query("DELETE FROM PublisherRefreshToken rt WHERE rt.revoked = true OR rt.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpiredAndRevoked();
}
