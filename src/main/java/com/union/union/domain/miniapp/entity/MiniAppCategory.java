package com.union.union.domain.miniapp.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mini_app_categories")
public class MiniAppCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String iconUrl;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public MiniAppCategory(String name, String displayName, String iconUrl, boolean isActive) {
        this.name = name;
        this.displayName = displayName;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }
}
