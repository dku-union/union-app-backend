package com.union.union.domain.auth.validator;

import com.union.union.domain.auth.exception.InvalidEmailFormatException;
import org.springframework.stereotype.Component;

@Component
public class EmailDomainValidator {

    /**
     * 이메일 주소에서 도메인 부분만 추출합니다.
     * @param email 검증할 이메일 문자열
     * @return 추출된 이메일 도메인 (소문자 변환)
     * @throws InvalidEmailFormatException 이메일 형식이 잘못되었을 때 발생
     */
    public String extractDomain(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailFormatException("이메일 주소가 비어있습니다.");
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            throw new InvalidEmailFormatException("올바른 이메일 형식이 아닙니다: " + email);
        }

        return email.substring(atIndex + 1).toLowerCase();
    }
}
