package com.union.union.domain.appversion.controller;

import com.union.union.domain.appversion.dto.AppVersionResponseDto;
import com.union.union.domain.appversion.dto.CreateVersionRequestDto;
import com.union.union.domain.appversion.dto.CreateVersionResponseDto;
import com.union.union.domain.appversion.service.AppVersionService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/app-versions")
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionService appVersionService;

    @PostMapping
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<CreateVersionResponseDto> createVersion(
            @Valid @RequestBody CreateVersionRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        CreateVersionResponseDto response = appVersionService.createVersion(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<AppVersionResponseDto> confirmUpload(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        AppVersionResponseDto response = appVersionService.confirmUpload(id, principal.userId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/test-complete")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<AppVersionResponseDto> markTested(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        AppVersionResponseDto response = appVersionService.markTested(id, principal.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/bundle")
    @PreAuthorize("hasRole('PUBLISHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getBundleUrl(
            @PathVariable UUID id
    ) {
        String downloadUrl = appVersionService.getBundleDownloadUrl(id);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    @GetMapping("/mini-app/{miniAppId}")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<List<AppVersionResponseDto>> getVersionsByMiniApp(
            @PathVariable Long miniAppId
    ) {
        List<AppVersionResponseDto> response = appVersionService.getVersionsByMiniApp(miniAppId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PUBLISHER') or hasRole('ADMIN')")
    public ResponseEntity<AppVersionResponseDto> getVersion(@PathVariable UUID id) {
        AppVersionResponseDto response = appVersionService.getVersion(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deploy")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<AppVersionResponseDto> deploy(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        AppVersionResponseDto response = appVersionService.deploy(id, principal.userId());
        return ResponseEntity.ok(response);
    }
}
