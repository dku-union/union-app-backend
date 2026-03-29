package com.union.union.domain.publisher.repository;

import com.union.union.domain.publisher.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    Optional<Publisher> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
