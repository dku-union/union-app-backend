package com.union.union.domain.notification.repository;

import com.union.union.domain.notification.entity.PublisherFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublisherFcmTokenRepository extends JpaRepository<PublisherFcmToken, Long> {

    @Query("SELECT t.token FROM PublisherFcmToken t WHERE t.publisher.publisherId = :publisherId")
    List<String> findTokensByPublisherId(@Param("publisherId") UUID publisherId);

    Optional<PublisherFcmToken> findByToken(String token);

    Optional<PublisherFcmToken> findByPublisher_PublisherIdAndDeviceId(UUID publisherId, String deviceId);

    @Modifying
    @Query("DELETE FROM PublisherFcmToken t WHERE t.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM PublisherFcmToken t WHERE t.publisher.publisherId = :publisherId AND t.deviceId = :deviceId")
    void deleteByPublisherIdAndDeviceId(@Param("publisherId") UUID publisherId, @Param("deviceId") String deviceId);
}
