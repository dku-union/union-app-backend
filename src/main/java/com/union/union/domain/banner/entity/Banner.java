package com.union.union.domain.banner.entity;

import com.union.union.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "banners", indexes = {
        @Index(name = "idx_banners_active_window", columnList = "isActive, startAt, endAt, sortOrder")
})
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(length = 8)
    private String emoji;

    @Column(length = 6)
    private String gradientStartHex;

    @Column(length = 6)
    private String gradientEndHex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerLinkType linkType;

    @Column(length = 500)
    private String linkTarget;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean isActive;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(length = 100)
    private String targetUniversity;

    @Builder
    public Banner(
            String imageUrl, String title, String subtitle,
            String emoji, String gradientStartHex, String gradientEndHex,
            BannerLinkType linkType, String linkTarget,
            Integer sortOrder, Boolean isActive,
            LocalDateTime startAt, LocalDateTime endAt, String targetUniversity
    ) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.subtitle = subtitle;
        this.emoji = emoji;
        this.gradientStartHex = gradientStartHex;
        this.gradientEndHex = gradientEndHex;
        this.linkType = linkType != null ? linkType : BannerLinkType.NONE;
        this.linkTarget = linkTarget;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isActive = isActive != null ? isActive : true;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetUniversity = targetUniversity;
    }

    public void activate()   { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}
