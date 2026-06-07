package com.union.union.domain.permission.service;

import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.permission.dto.MiniAppPermissionStateDto;
import com.union.union.domain.permission.dto.PermissionDecisionDto;
import com.union.union.domain.permission.dto.PermissionItemDto;
import com.union.union.domain.permission.dto.UserPermissionGroupDto;
import com.union.union.domain.permission.entity.MiniAppUserPermission;
import com.union.union.domain.permission.repository.MiniAppUserPermissionRepository;
import com.union.union.global.common.exception.BadRequestException;
import com.union.union.global.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MiniAppPermissionService {

    private final MiniAppUserPermissionRepository permissionRepository;
    private final MiniAppRepository miniAppRepository;

    /**
     * (a) 미니앱이 선언한 스코프 + 현재 사용자의 결정을 조인해 반환한다.
     * 선언됐으나 결정이 없는 스코프는 {@code hasDecision=false, granted=false}(default-deny).
     */
    @Transactional(readOnly = true)
    public MiniAppPermissionStateDto getPermissionState(UUID userId, Long miniAppId) {
        MiniApp app = miniAppRepository.findById(miniAppId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. id=" + miniAppId));

        List<PermissionScope> declared = PermissionScope.sanitize(app.getPermissions());
        Map<PermissionScope, Boolean> decisions = decisionMap(userId, miniAppId);

        List<PermissionItemDto> items = declared.stream()
                .map(scope -> toItem(scope, decisions))
                .toList();

        return new MiniAppPermissionStateDto(app.getId(), app.getAppId(), items);
    }

    /**
     * (b) 사용자의 권한 결정을 배치 업서트한다. 미니앱이 선언하지 않은 스코프는 400.
     */
    public MiniAppPermissionStateDto updateDecisions(UUID userId, Long miniAppId,
                                                     List<PermissionDecisionDto> decisions) {
        MiniApp app = miniAppRepository.findById(miniAppId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. id=" + miniAppId));

        Set<PermissionScope> declared = new HashSet<>(PermissionScope.sanitize(app.getPermissions()));

        for (PermissionDecisionDto decision : decisions) {
            PermissionScope scope = PermissionScope.fromNullable(decision.scope());
            if (scope == null || !declared.contains(scope)) {
                throw new BadRequestException("미니앱이 선언하지 않은 권한입니다: " + decision.scope());
            }
            upsertDecision(userId, miniAppId, scope, decision.granted());
        }
        log.info("권한 결정 업서트. userId={}, miniAppId={}, count={}", userId, miniAppId, decisions.size());
        return getPermissionState(userId, miniAppId);
    }

    /**
     * (c) 사용자의 전체 권한 결정을 미니앱 단위로 묶어 반환한다(권한 관리 화면).
     * 선언 스코프 + 선언엔 없지만 결정이 남은 stale 스코프까지 노출(철회 가능).
     */
    @Transactional(readOnly = true)
    public List<UserPermissionGroupDto> listUserDecisions(UUID userId) {
        List<MiniAppUserPermission> rows = permissionRepository.findByUserId(userId);
        if (rows.isEmpty()) return List.of();

        Map<Long, Map<PermissionScope, Boolean>> byApp = new LinkedHashMap<>();
        for (MiniAppUserPermission row : rows) {
            byApp.computeIfAbsent(row.getMiniAppId(), k -> new LinkedHashMap<>())
                    .put(row.getScope(), row.isGranted());
        }

        Map<Long, MiniApp> apps = miniAppRepository.findAllById(byApp.keySet()).stream()
                .collect(Collectors.toMap(MiniApp::getId, a -> a));

        List<UserPermissionGroupDto> result = new ArrayList<>();
        for (Map.Entry<Long, Map<PermissionScope, Boolean>> entry : byApp.entrySet()) {
            MiniApp app = apps.get(entry.getKey());
            if (app == null) continue; // 삭제된 미니앱의 잔여 결정은 노출하지 않음

            Map<PermissionScope, Boolean> decisions = entry.getValue();

            // 선언 스코프 우선, 이어서 선언엔 없지만 결정이 남은 stale 스코프 (순서 보존)
            LinkedHashSet<PermissionScope> scopes = new LinkedHashSet<>(PermissionScope.sanitize(app.getPermissions()));
            scopes.addAll(decisions.keySet());

            List<PermissionItemDto> items = scopes.stream()
                    .map(scope -> toItem(scope, decisions))
                    .toList();

            result.add(new UserPermissionGroupDto(
                    app.getId(), app.getAppId(), app.getName(), app.getIconUrl(), items));
        }
        return result;
    }

    /**
     * (d) 사용자가 해당 미니앱에 실제로 동의(granted=true)한 스코프 집합을 반환한다.
     * ID 토큰 발급 시 어떤 신원 claim 을 담을지 결정하는 데 쓴다.
     */
    @Transactional(readOnly = true)
    public Set<PermissionScope> getGrantedScopes(UUID userId, Long miniAppId) {
        return permissionRepository.findByUserIdAndMiniAppId(userId, miniAppId).stream()
                .filter(MiniAppUserPermission::isGranted)
                .map(MiniAppUserPermission::getScope)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // --- helpers ---

    private Map<PermissionScope, Boolean> decisionMap(UUID userId, Long miniAppId) {
        return permissionRepository.findByUserIdAndMiniAppId(userId, miniAppId).stream()
                .collect(Collectors.toMap(MiniAppUserPermission::getScope,
                        MiniAppUserPermission::isGranted, (a, b) -> b));
    }

    private PermissionItemDto toItem(PermissionScope scope, Map<PermissionScope, Boolean> decisions) {
        boolean hasDecision = decisions.containsKey(scope);
        return new PermissionItemDto(scope.getValue(), hasDecision, decisions.getOrDefault(scope, false));
    }

    /**
     * find-then-update/insert 업서트. 동일 사용자가 단일 기기에서 순차 호출하므로 경합은 사실상 없고,
     * 극히 드문 동시 삽입은 {@code uk_miniapp_user_perm} unique 제약이 최종 보루다(SubscriptionService 패턴과 동일).
     */
    private void upsertDecision(UUID userId, Long miniAppId, PermissionScope scope, boolean granted) {
        permissionRepository.findByUserIdAndMiniAppIdAndScope(userId, miniAppId, scope)
                .ifPresentOrElse(
                        existing -> existing.updateGranted(granted),
                        () -> permissionRepository.save(MiniAppUserPermission.builder()
                                .userId(userId)
                                .miniAppId(miniAppId)
                                .scope(scope)
                                .granted(granted)
                                .build())
                );
    }
}
