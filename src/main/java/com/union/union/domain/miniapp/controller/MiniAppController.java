package com.union.union.domain.miniapp.controller;

import com.union.union.domain.miniapp.dto.MiniAppRegisterRequestDto;
import com.union.union.domain.miniapp.dto.MiniAppResponseDto;
import com.union.union.domain.miniapp.service.MiniAppService;
import com.union.union.global.infra.gcs.GcsService;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mini-apps")
@RequiredArgsConstructor
public class MiniAppController {

    private final MiniAppService miniAppService;
    private final GcsService gcsService;

    @PostMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<MiniAppResponseDto> register(
            @Valid @RequestBody MiniAppRegisterRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        MiniAppResponseDto response = miniAppService.register(request, principal.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MiniAppResponseDto>> getMiniApps(
            @RequestParam(required = false) Long universityId
    ) {
        List<MiniAppResponseDto> response = miniAppService.getApprovedMiniApps(universityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MiniAppResponseDto>> getPopularMiniApps(
            @RequestParam(defaultValue = "10") int limit
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

    @GetMapping("/{id}/launch")
    public ResponseEntity<Void> launch(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.userId() : null;
        String launchUrl = miniAppService.getLaunchUrl(id, userId);
        
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(launchUrl))
                .build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MiniAppResponseDto> approve(@PathVariable Long id) {
        MiniAppResponseDto response = miniAppService.approve(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signed-url")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<GcsSignedUrlResponseDto> getSignedUrl(
            @RequestParam String filename,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        GcsSignedUrlResponseDto response = gcsService.getMiniAppSignedUrl(principal.userId(), filename);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<java.util.Map<String, String>> getDownloadUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.userId() : null;
        String launchUrl = miniAppService.getLaunchUrl(id, userId);

        // launchUrl이 GCS 경로를 포함하는 경우 CDN Signed URL로 변환
        String cdnUrl = gcsService.getCdnDownloadUrl(extractObjectPath(launchUrl));
        return ResponseEntity.ok(java.util.Map.of("downloadUrl", cdnUrl));
    }

    private String extractObjectPath(String url) {
        // GCS URL 또는 상대 경로에서 오브젝트 경로 추출
        if (url.contains("storage.googleapis.com/")) {
            return url.substring(url.indexOf("storage.googleapis.com/") + "storage.googleapis.com/".length());
        }
        if (url.startsWith("mini-apps/")) {
            return url;
        }
        return url.startsWith("/") ? url.substring(1) : url;
    }
}
