package com.union.union.domain.permission.controller;

import com.union.union.domain.permission.dto.MiniAppPermissionStateDto;
import com.union.union.domain.permission.dto.UpdatePermissionsRequestDto;
import com.union.union.domain.permission.dto.UserPermissionGroupDto;
import com.union.union.domain.permission.service.MiniAppPermissionService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 미니앱별 사용자 권한 결정 API.
 * 모든 엔드포인트는 JWT 인증 필요(SecurityConfig: /api/v1/users/me/** authenticated).
 */
@RestController
@RequiredArgsConstructor
public class MiniAppPermissionController {

    private final MiniAppPermissionService permissionService;

    /** (a) 미니앱 선언 권한 + 현재 사용자의 결정 조회 (최초 접속 게이트). */
    @GetMapping("/api/v1/users/me/miniapps/{miniAppId}/permissions")
    public ResponseEntity<MiniAppPermissionStateDto> getPermissionState(
            @PathVariable Long miniAppId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(permissionService.getPermissionState(principal.userId(), miniAppId));
    }

    /** (b) 권한 결정 배치 업서트 (동의 모달 / 권한 관리 토글). */
    @PutMapping("/api/v1/users/me/miniapps/{miniAppId}/permissions")
    public ResponseEntity<MiniAppPermissionStateDto> updatePermissions(
            @PathVariable Long miniAppId,
            @Valid @RequestBody UpdatePermissionsRequestDto request,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                permissionService.updateDecisions(principal.userId(), miniAppId, request.decisions()));
    }

    /** (c) 사용자의 전체 권한 결정 목록 (권한 관리 화면). */
    @GetMapping("/api/v1/users/me/permissions")
    public ResponseEntity<List<UserPermissionGroupDto>> listUserPermissions(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return ResponseEntity.ok(permissionService.listUserDecisions(principal.userId()));
    }
}
