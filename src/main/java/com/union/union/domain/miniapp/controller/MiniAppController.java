package com.union.union.domain.miniapp.controller;

import com.union.union.domain.miniapp.dto.*;
import com.union.union.domain.miniapp.service.MiniAppSearchService;
import com.union.union.domain.miniapp.service.MiniAppService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/mini-apps")
@RequiredArgsConstructor
public class MiniAppController {

    private final MiniAppService miniAppService;
    private final MiniAppSearchService miniAppSearchService;

    @PostMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<MiniAppResponseDto> register(
            @Valid @RequestBody MiniAppRegisterRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        MiniAppResponseDto response = miniAppService.register(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MiniAppResponseDto>> getMiniApps() {
        // TODO: 대학교별 필터링 (university 확정 후 구현)
        List<MiniAppResponseDto> response = miniAppService.getApprovedMiniApps();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MiniAppResponseDto>> getPopularMiniApps(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<MiniAppResponseDto> response = miniAppService.getPopularMiniApps(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<MiniAppResponseDto>> getRecommendations(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<MiniAppResponseDto> response = miniAppService.getRecommendedMiniApps(principal.userId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/launch")
    public ResponseEntity<Map<String, String>> launch(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        String bundleUrl = miniAppService.getLaunchUrl(id, principal.userId());
        return ResponseEntity.ok(Map.of("bundleUrl", bundleUrl));
    }

    @GetMapping("/discovery")
    public ResponseEntity<DiscoveryResponseDto> getDiscoveryApps(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.userId() : null;
        DiscoveryResponseDto response = miniAppService.getDiscoveryData(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MiniAppLiteDto>> searchMiniApps(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        List<MiniAppLiteDto> response = miniAppSearchService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MiniAppLiteDto>> getMiniAppsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        List<MiniAppLiteDto> response = miniAppSearchService.searchByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/popular")
    public ResponseEntity<List<String>> getPopularKeywords() {
        return ResponseEntity.ok(miniAppSearchService.getPopularKeywords(5));
    }
}
