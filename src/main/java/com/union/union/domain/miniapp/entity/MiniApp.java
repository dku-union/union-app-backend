package com.union.union.domain.miniapp.entity;

import com.union.union.domain.workspace.entity.Workspace;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mini_apps")
public class MiniApp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String iconUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private MiniAppCategory category;

    @Column(length = 200)
    private String tags;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MiniAppStatus status;

    /**
     * 미니앱 식별자 (reverse-domain, e.g. "com.union.soccer").
     * union.config.json 의 appId 와 동일. Analytics 이벤트 매핑 키 + 알림 발송 targetAppId.
     * 모든 신규/기존 앱에 필수 (레거시 row 는 com.union.legacy-{id} placeholder 로 마이그레이션됨).
     */
    @Column(name = "app_id", unique = true, length = 100, nullable = false)
    private String appId;

    @Column(name = "searchable_name", length = 300)
    private String searchableName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private List<PermissionScope> permissions;

    @Builder
    public MiniApp(String name, String description, String iconUrl, MiniAppCategory category, String tags,
                   Workspace workspace, MiniAppStatus status, String appId, List<PermissionScope> permissions) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.category = category;
        this.tags = tags;
        this.workspace = workspace;
        this.status = status != null ? status : MiniAppStatus.PENDING;
        this.appId = appId;
        this.permissions = permissions;
    }

    @PrePersist
    @PreUpdate
    public void syncSearchableName() {
        if (this.name != null) {
            this.searchableName = com.union.union.domain.miniapp.utils.KoreanUtils.decompose(this.name);
        }
    }

    public void updateAppId(String appId) {
        this.appId = appId;
    }

    public void updateIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void updateMeta(String name, String description) {
        if (name != null) this.name = name;
        this.description = description;
    }

    public void approve() {
        this.status = MiniAppStatus.APPROVED;
    }

    public void reject() {
        this.status = MiniAppStatus.REJECTED;
    }
}
