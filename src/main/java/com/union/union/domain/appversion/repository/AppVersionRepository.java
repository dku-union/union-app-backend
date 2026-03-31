package com.union.union.domain.appversion.repository;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {

    @EntityGraph(attributePaths = {"miniApp", "publisher"})
    List<AppVersion> findByMiniAppIdOrderByCreatedAtDesc(Long miniAppId);

    @EntityGraph(attributePaths = {"miniApp", "publisher"})
    List<AppVersion> findByPublisherIdOrderByCreatedAtDesc(UUID publisherId);

    @EntityGraph(attributePaths = {"miniApp", "publisher"})
    List<AppVersion> findByStatus(VersionStatus status);

    @EntityGraph(attributePaths = {"miniApp", "publisher"})
    Optional<AppVersion> findDetailedById(UUID id);
}
