package com.union.union.domain.miniapp.entity;

import com.union.union.domain.workspace.entity.Workspace;
import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MiniAppCategory category;

    @Column(length = 200)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MiniAppStatus status;

    @Builder
    public MiniApp(String name, String description, String iconUrl, MiniAppCategory category, String tags, Workspace workspace, MiniAppStatus status) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.category = category != null ? category : MiniAppCategory.ETC;
        this.tags = tags;
        this.workspace = workspace;
        this.status = status != null ? status : MiniAppStatus.PENDING;
    }

    public void approve() {
        this.status = MiniAppStatus.APPROVED;
    }

public void reject() {
        this.status = MiniAppStatus.REJECTED;
    }
}
