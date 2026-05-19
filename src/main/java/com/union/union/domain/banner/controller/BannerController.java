package com.union.union.domain.banner.controller;

import com.union.union.domain.banner.dto.BannerResponseDto;
import com.union.union.domain.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * Home 캐러셀용 활성 배너 목록.
     * GET /banners — public, 5분 캐시.
     */
    @GetMapping
    public ResponseEntity<List<BannerResponseDto>> getBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }
}
