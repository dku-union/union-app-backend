package com.union.union.domain.appversion.repository;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.entity.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {

    @Query("SELECT v FROM AppVersion v JOIN FETCH v.miniApp m JOIN FETCH m.workspace WHERE v.miniApp.id = :miniAppId ORDER BY v.createdAt DESC")
    List<AppVersion> findByMiniAppIdOrderByCreatedAtDesc(@Param("miniAppId") Long miniAppId);

    @Query("SELECT v FROM AppVersion v JOIN FETCH v.miniApp m JOIN FETCH m.workspace WHERE v.status = :status")
    List<AppVersion> findByStatus(@Param("status") VersionStatus status);

    @Query("SELECT v FROM AppVersion v JOIN FETCH v.miniApp m JOIN FETCH m.workspace WHERE v.id = :id")
    Optional<AppVersion> findDetailedById(@Param("id") UUID id);

    Optional<AppVersion> findFirstByMiniAppIdAndStatusOrderByCreatedAtDesc(Long miniAppId, VersionStatus status);
}
