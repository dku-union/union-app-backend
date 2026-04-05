package com.union.union.domain.publisher.controller;

import com.union.union.domain.miniapp.dto.MiniAppResponseDto;
import com.union.union.domain.miniapp.service.MiniAppService;
import com.union.union.domain.miniapp.usage.dto.PublisherStatisticsResponseDto;
import com.union.union.domain.workspace.service.WorkspaceAuthorizationService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final MiniAppService miniAppService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    @GetMapping("/me/mini-apps")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<List<MiniAppResponseDto>> getMyMiniApps(
            @RequestParam UUID workspaceId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        workspaceAuthorizationService.validateMembership(workspaceId, principal.userId());
        List<MiniAppResponseDto> response = miniAppService.getMiniAppsByWorkspace(workspaceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<PublisherStatisticsResponseDto> getStatistics(
            @RequestParam UUID workspaceId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        workspaceAuthorizationService.validateMembership(workspaceId, principal.userId());
        PublisherStatisticsResponseDto response = miniAppService.getWorkspaceStatistics(workspaceId);
        return ResponseEntity.ok(response);
    }

    // TODO: Publisher apply/approve 로직은 Next.js (NextAuth) 기반으로 전환됨. User-Publisher 혼동 코드로 사용하지 않음.
    // @PostMapping("/apply")
    // public ResponseEntity<String> apply(
    //         @Valid @RequestBody PublisherApplyRequestDto request,
    //         @AuthenticationPrincipal JwtUserPrincipal principal
    // ) {
    //     publisherService.apply(request, principal.userId());
    //     return ResponseEntity.ok("퍼블리셔 신청이 완료되었으며, 역할이 PUBLISHER로 변경되었습니다.");
    // }

    // @PatchMapping("/{id}/approve")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<String> approve(@PathVariable Long id) {
    //     publisherService.approve(id);
    //     return ResponseEntity.ok("퍼블리셔 승인이 완료되었습니다.");
    // }
}
