package com.union.union.domain.publisher.repository;

import com.union.union.domain.publisher.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByPublisher_PublisherIdOrderByCreatedAtDesc(UUID publisherId);

    Optional<ApiKey> findByIdAndPublisher_PublisherId(Long id, UUID publisherId);
}
