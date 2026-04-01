package com.union.union.domain.miniapp.controller;

import com.union.union.domain.miniapp.dto.MiniAppRegisterRequestDto;
import com.union.union.domain.miniapp.dto.MiniAppResponseDto;
import com.union.union.domain.miniapp.service.MiniAppService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mini-apps")
@RequiredArgsConstructor
public class MiniAppController {

    private final MiniAppService miniAppService;

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
    public ResponseEntity<List<MiniAppResponseDto>> getMiniApps() {
        // TODO: 대학교별 필터링 (university 확정 후 구현)
        List<MiniAppResponseDto> response = miniAppService.getApprovedMiniApps();
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

    @PostMapping("/{id}/launch")
    public ResponseEntity<Map<String, String>> launch(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.userId() : null;
        String bundleUrl = miniAppService.getLaunchUrl(id, userId);

        return ResponseEntity.ok(Map.of("bundleUrl", bundleUrl));
    }

}
