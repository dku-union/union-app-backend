package com.union.union.domain.auth.service;

import com.union.union.domain.auth.store.EmailVerificationStore;
import com.union.union.domain.university.entity.UniversityDomain;
import com.union.union.global.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationStore verificationStore;
    private final MailService mailService;
    private final EmailDomainValidationService emailDomainValidationService;

    private static final long EXPIRE_TIME = 5 * 60 * 1000L; // 5분

    /**
     * 이메일 도메인을 검증하고 인증 코드를 발송합니다.
     * @return 자동 판별된 대학교명
     */
    public String sendCode(String email) {
        UniversityDomain university = emailDomainValidationService.validateAndResolve(email);

        String code = generateCode();
        verificationStore.save(email, code, System.currentTimeMillis() + EXPIRE_TIME);

        mailService.sendVerificationEmail(email, code);
        log.info("인증 코드 발송 완료. email: {}", email);

        return university.getUniversityName();
    }

    /**
     * 인증 코드 검증
     */
    public boolean verifyCode(String email, String code) {
        String savedCode = verificationStore.get(email)
                .orElseThrow(() -> new BadRequestException("만료되었거나 유효하지 않은 인증 정보입니다."));

        if (!savedCode.equals(code)) {
            throw new BadRequestException("인증번호가 일치하지 않습니다.");
        }

        verificationStore.delete(email);
        verificationStore.markAsVerified(email);
        log.info("이메일 인증 성공. email: {}", email);
        return true;
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }
}
