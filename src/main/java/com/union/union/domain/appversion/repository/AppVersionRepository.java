package com.union.union.domain.appversion.repository;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {

    List<AppVersion> findByMiniAppIdOrderByCreatedAtDesc(Long miniAppId);

    List<AppVersion> findByPublisherIdOrderByCreatedAtDesc(UUID publisherId);

    List<AppVersion> findByStatus(VersionStatus status);
}
