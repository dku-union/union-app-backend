package com.union.union.domain.publisher.auth.service;

import com.union.union.domain.auth.service.MailService;
import com.union.union.domain.publisher.auth.dto.PublisherEmailSendResponseDto;
import com.union.union.domain.publisher.auth.dto.PublisherTokenResponseDto;
import com.union.union.domain.publisher.auth.entity.PublisherRefreshToken;
import com.union.union.domain.publisher.auth.exception.PublisherInactiveException;
import com.union.union.domain.publisher.auth.exception.PublisherNotRegisteredException;
import com.union.union.domain.publisher.auth.repository.PublisherRefreshTokenRepository;
import com.union.union.domain.publisher.auth.store.PublisherEmailVerificationStore;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.entity.PublisherStatus;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.global.common.exception.BadRequestException;
import com.union.union.global.common.exception.InvalidRefreshTokenException;
import com.union.union.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * iOS 퍼블리셔 패스워드리스 로그인 (이메일 OTP 기반).
 *
 * <p>플로우:
 * <ol>
 *     <li>{@link #sendCode(String)} — 등록된 퍼블리셔에게만 6자리 OTP 메일 발송</li>
 *     <li>{@link #verifyAndIssueTokens(String, String)} — OTP 검증 후 access/refresh 발급</li>
 * </ol>
 *
 * <p>설계 노트:
 * <ul>
 *     <li>존재하지 않는 이메일은 {@link PublisherNotRegisteredException}로 명시 응답
 *         — 가입 유도 UX가 핵심 요구사항. 사용자 enumeration 위험은 rate-limit으로 완화.</li>
 *     <li>OTP는 5분 만료. 검증 성공 시 즉시 삭제하여 재사용 불가.</li>
 *     <li>refresh token은 별도 테이블({@code publisher_refresh_tokens})에 저장하여
 *         사용자(User) 세션과 정책·수명을 독립적으로 운영한다.</li>
 *     <li>발급되는 JWT의 {@code sub}는 {@link Publisher#getPublisherId()} (UUID).
 *         기존 워크스페이스 권한 검증({@link com.union.union.domain.workspace.service.WorkspaceAuthorizationService})이
 *         {@code principal.userId()}를 publisherId로 사용하므로 그대로 호환된다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherAuthService {

    private static final long CODE_TTL_MILLIS = 5 * 60 * 1000L; // 5분
    private static final int CODE_TTL_SECONDS = (int) (CODE_TTL_MILLIS / 1000);

    private final PublisherRepository publisherRepository;
    private final PublisherEmailVerificationStore verificationStore;
    private final PublisherRefreshTokenRepository refreshTokenRepository;
    private final MailService mailService;
    private final JwtProvider jwtProvider;
    private final SecureRandom random = new SecureRandom();

    /** 로컬 개발 시 검증·테스트 편의를 위해 OTP를 콘솔에 직접 출력. 운영에선 false. */
    @Value("${mail.dev.log-only:false}")
    private boolean logOtpForDev;

    /**
     * 등록된 퍼블리셔에게만 OTP를 발송한다.
     *
     * @throws PublisherNotRegisteredException 미등록 이메일
     * @throws PublisherInactiveException      ACTIVE가 아닌 상태
     */
    public PublisherEmailSendResponseDto sendCode(String email) {
        String normalized = email.trim().toLowerCase();
        Publisher publisher = publisherRepository.findByEmail(normalized)
                .orElseThrow(PublisherNotRegisteredException::new);
        ensureActive(publisher);

        String code = generateCode();
        verificationStore.delete(normalized);
        verificationStore.save(normalized, code, System.currentTimeMillis() + CODE_TTL_MILLIS);
        mailService.sendVerificationEmail(normalized, code);

        if (logOtpForDev) {
            log.info("[PublisherAuth/dev] email={}, otp={}", normalized, code);
        }
        log.info("[PublisherAuth] OTP 발송 완료. publisherId={}", publisher.getPublisherId());
        return new PublisherEmailSendResponseDto(CODE_TTL_SECONDS, EmailMasker.mask(normalized));
    }

    /**
     * OTP를 검증하고 토큰을 발급한다. 성공 시 OTP는 즉시 무효화한다.
     */
    @Transactional
    public PublisherTokenResponseDto verifyAndIssueTokens(String email, String code) {
        String normalized = email.trim().toLowerCase();

        String savedCode = verificationStore.get(normalized)
                .orElseThrow(() -> new BadRequestException("만료되었거나 발급되지 않은 인증 정보입니다."));
        if (!savedCode.equals(code)) {
            throw new BadRequestException("인증번호가 일치하지 않습니다.");
        }
        verificationStore.delete(normalized);

        Publisher publisher = publisherRepository.findByEmail(normalized)
                .orElseThrow(PublisherNotRegisteredException::new);
        ensureActive(publisher);

        return issueTokens(publisher);
    }

    /**
     * 만료 직전 토큰을 갱신한다. 재사용 감지 시 해당 퍼블리셔의 모든 토큰을 무효화.
     */
    @Transactional
    public PublisherTokenResponseDto refresh(String refreshToken) {
        PublisherRefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 refresh token입니다"));

        if (stored.isRevoked()) {
            log.warn("[PublisherAuth] Refresh token 재사용 감지! publisherId={}. 전체 세션 무효화.",
                    stored.getPublisherUuid());
            refreshTokenRepository.revokeAllByPublisherId(stored.getPublisherUuid());
            throw new InvalidRefreshTokenException("보안상 모든 세션이 로그아웃되었습니다. 다시 로그인해주세요.");
        }
        if (stored.isExpired()) {
            stored.revoke();
            throw new InvalidRefreshTokenException("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        stored.revoke();
        Publisher publisher = stored.getPublisher();
        ensureActive(publisher);

        log.info("[PublisherAuth] 토큰 갱신. publisherId={}", publisher.getPublisherId());
        return issueTokens(publisher);
    }

    @Transactional
    public void logout(java.util.UUID publisherId) {
        refreshTokenRepository.revokeAllByPublisherId(publisherId);
        log.info("[PublisherAuth] 로그아웃. publisherId={}", publisherId);
    }

    // ── helpers ──────────────────────────────────────────────

    private void ensureActive(Publisher publisher) {
        if (publisher.getPubstatus() != PublisherStatus.ACTIVE) {
            throw new PublisherInactiveException(
                    "퍼블리셔 계정이 활성 상태가 아닙니다. (상태: " + publisher.getPubstatus() + ")"
            );
        }
    }

    private PublisherTokenResponseDto issueTokens(Publisher publisher) {
        String accessToken = jwtProvider.createAccessToken(
                publisher.getPublisherId(),
                publisher.getEmail(),
                "ROLE_PUBLISHER"
        );
        String refreshTokenStr = jwtProvider.createRefreshToken(publisher.getPublisherId());

        long refreshTtlMillis = jwtProvider.getRefreshTokenExpirationMillis();
        PublisherRefreshToken entity = PublisherRefreshToken.builder()
                .token(refreshTokenStr)
                .publisher(publisher)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTtlMillis / 1000))
                .build();
        refreshTokenRepository.save(entity);

        return new PublisherTokenResponseDto(
                accessToken,
                refreshTokenStr,
                publisher.getPublisherId(),
                publisher.getName(),
                "ROLE_PUBLISHER"
        );
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
