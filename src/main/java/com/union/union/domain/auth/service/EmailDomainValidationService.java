package com.union.union.domain.auth.service;

import com.union.union.domain.auth.exception.InvalidUniversityDomainException;
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
     * 이메일 주소의 도메인이 허용된 대학교 이메일인지 검증합니다.
    public void validate(String email) {
        // 1. 형식 검증
        emailFormatValidator.validate(email);
        
        // 2. 도메인 추출
        String domain = emailDomainValidator.extractDomain(email);

        // 3. DB에 등록된 대학교 도메인인지 확인
        if (!universityDomainRepository.existsByDomain(domain)) {
            throw new InvalidUniversityDomainException("허용되지 않은 대학교 이메일 도메인입니다: " + domain);
        }
    }
}
