package com.union.union.domain.miniapp.service;

import com.union.union.domain.miniapp.dto.IdTokenResponseDto;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.entity.PermissionScope;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.permission.service.MiniAppPermissionService;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.common.exception.EntityNotFoundException;
import com.union.union.global.common.exception.UnauthorizedAccessException;
import com.union.union.global.security.jwt.AppIdTokenProvider;
import com.union.union.global.security.jwt.IdTokenProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * 미니앱 ID 토큰 발급 서비스.
 *
 * <p>세션 토큰(HS256)을 미니앱에 노출하던 기존 방식을 대체한다. native(iOS) 가 사용자 세션으로
 * 인증해 호출하면, 해당 미니앱(appId)에 스코프된 ID 토큰(RS256)을 발급한다. publisher 백엔드는
 * 이 토큰을 JWKS 로 검증해 사용자 신원을 확인한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MiniAppIdTokenService {

    private final MiniAppRepository miniAppRepository;
    private final UserRepository userRepository;
    private final MiniAppPermissionService permissionService;
    private final AppIdTokenProvider idTokenProvider;
    private final IdTokenProperties idTokenProperties;

    public IdTokenResponseDto issue(Long miniAppId, UUID userId) {
        MiniApp miniApp = miniAppRepository.findById(miniAppId)
                .orElseThrow(() -> new EntityNotFoundException("MiniApp을 찾을 수 없습니다. id=" + miniAppId));

        // launch 와 동일한 접근 게이트: 승인(배포)된 미니앱에 대해서만 ID 토큰을 발급한다.
        // 이게 없으면 인증된 아무 사용자나 임의 미니앱(미승인/비공개 포함) 스코프의 토큰을 발급받을 수 있다.
        if (miniApp.getStatus() != MiniAppStatus.APPROVED) {
            throw new UnauthorizedAccessException("배포되지 않은 MiniApp입니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        Set<PermissionScope> granted = permissionService.getGrantedScopes(userId, miniAppId);
        String idToken = idTokenProvider.createIdToken(user, miniApp.getAppId(), granted);

        log.info("미니앱 ID 토큰 발급. miniAppId={}, appId={}, userId={}, grantedScopes={}",
                miniAppId, miniApp.getAppId(), userId, granted);
        return new IdTokenResponseDto(idToken, "Bearer", idTokenProperties.expiration() / 1000);
    }
}
