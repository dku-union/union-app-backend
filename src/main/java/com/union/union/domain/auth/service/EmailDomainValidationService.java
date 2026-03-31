package com.union.union.domain.auth.service;

import com.union.union.domain.auth.exception.InvalidUniversityDomainException;
import com.union.union.domain.university.entity.UniversityDomain;
import com.union.union.domain.university.repository.UniversityDomainRepository;
import com.union.union.domain.auth.validator.EmailDomainValidator;
import com.union.union.domain.auth.validator.EmailFormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailDomainValidationService {

    private final EmailFormatValidator emailFormatValidator;
    private final EmailDomainValidator emailDomainValidator;
    private final UniversityDomainRepository universityDomainRepository;

    /**
     * 이메일 도메인으로 대학교를 자동 판별하고 반환합니다.
     * @return 매칭된 UniversityDomain
     * @throws InvalidUniversityDomainException 등록되지 않은 대학교 도메인인 경우
     */
    public UniversityDomain validateAndResolve(String email) {
        // 1. 형식 검증
        emailFormatValidator.validate(email);

        // 2. 도메인 추출
        String domain = emailDomainValidator.extractDomain(email);

        // 3. DB에서 대학교 조회
        return universityDomainRepository.findByDomain(domain)
                .orElseThrow(() -> new InvalidUniversityDomainException(
                        "등록되지 않은 대학교 이메일입니다: " + domain));
    }
}
