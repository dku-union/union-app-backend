package com.union.union.domain.publisher.controller;

import com.union.union.domain.miniapp.dto.MiniAppResponseDto;
import com.union.union.domain.miniapp.service.MiniAppService;
import com.union.union.domain.miniapp.usage.dto.PublisherStatisticsResponseDto;
import com.union.union.domain.publisher.dto.PublisherApplyRequestDto;
import com.union.union.domain.publisher.service.PublisherService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;
    private final MiniAppService miniAppService;

    @GetMapping("/me/mini-apps")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<List<MiniAppResponseDto>> getMyMiniApps(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        List<MiniAppResponseDto> response = miniAppService.getMiniAppsByPublisher(principal.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<PublisherStatisticsResponseDto> getStatistics(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        PublisherStatisticsResponseDto response = miniAppService.getPublisherStatistics(principal.userId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply")
    public ResponseEntity<String> apply(
            @Valid @RequestBody PublisherApplyRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        publisherService.apply(request, principal.userId());
        return ResponseEntity.ok("퍼블리셔 신청이 완료되었으며, 역할이 PUBLISHER로 변경되었습니다.");
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        publisherService.approve(id);
        return ResponseEntity.ok("퍼블리셔 승인이 완료되었습니다.");
    }
}
