package com.union.union.domain.permission.repository;

import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.domain.permission.entity.MiniAppUserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MiniAppUserPermissionRepository extends JpaRepository<MiniAppUserPermission, Long> {

    List<MiniAppUserPermission> findByUserIdAndMiniAppId(UUID userId, Long miniAppId);

    List<MiniAppUserPermission> findByUserId(UUID userId);

    Optional<MiniAppUserPermission> findByUserIdAndMiniAppIdAndScope(UUID userId, Long miniAppId, PermissionScope scope);
}
