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
     * 이메일 주소의 도메인이 선택한 대학교의 허용된 이메일 도메인인지 검증합니다.
     */
    public void validate(String email, Long universityId) {
        // 1. 형식 검증
        emailFormatValidator.validate(email);
        
        // 2. 대학교 정보 조회
        UniversityDomain universityDomain = universityDomainRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대학교입니다."));

        // 3. 도메인 추출 및 검증
        String domain = emailDomainValidator.extractDomain(email);

        if (!domain.equals(universityDomain.getDomain())) {
            throw new InvalidUniversityDomainException("선택한 대학교의 이메일 도메인과 일치하지 않습니다: " + domain);
        }
    }
}
