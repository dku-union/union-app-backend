package com.union.union.domain.permission.entity;

import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 사용자(user) × 미니앱(miniApp) × 권한 스코프(scope) 별 동의 결정.
 *
 * <p>기존 {@code users} / {@code mini_apps} 테이블과의 스키마 충돌을 피하기 위해
 * JPA 연관관계(@ManyToOne) 대신 plain {@code userId}(UUID) / {@code miniAppId}(Long) 컬럼으로
 * 느슨하게 참조한다. 별도 테이블이므로 {@code ddl-auto: update} 에서 안전하게 신규 생성된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mini_app_user_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_miniapp_user_perm",
                columnNames = {"user_id", "mini_app_id", "scope"}
        ),
        indexes = {
                @Index(name = "idx_miniapp_user_perm_user", columnList = "user_id"),
                @Index(name = "idx_miniapp_user_perm_user_app", columnList = "user_id, mini_app_id")
        }
)
public class MiniAppUserPermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "mini_app_id", nullable = false)
    private Long miniAppId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 30)
    private PermissionScope scope;

    @Column(name = "granted", nullable = false)
    private boolean granted;

    @Builder
    public MiniAppUserPermission(UUID userId, Long miniAppId, PermissionScope scope, boolean granted) {
        this.userId = userId;
        this.miniAppId = miniAppId;
        this.scope = scope;
        this.granted = granted;
    }

    public void updateGranted(boolean granted) {
        this.granted = granted;
    }
}
