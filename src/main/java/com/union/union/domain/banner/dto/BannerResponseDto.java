package com.union.union.domain.banner.dto;

import com.union.union.domain.banner.entity.Banner;
import com.union.union.domain.banner.entity.BannerLinkType;

public record BannerResponseDto(
        Long id,
        String imageUrl,
        String title,
        String subtitle,
        String emoji,
        String gradientStartHex,
        String gradientEndHex,
        BannerLinkType linkType,
        String linkTarget
) {
    public static BannerResponseDto from(Banner b) {
        return new BannerResponseDto(
                b.getId(),
                b.getImageUrl(),
                b.getTitle(),
                b.getSubtitle(),
                b.getEmoji(),
                b.getGradientStartHex(),
                b.getGradientEndHex(),
                b.getLinkType(),
                b.getLinkTarget()
        );
    }
}
