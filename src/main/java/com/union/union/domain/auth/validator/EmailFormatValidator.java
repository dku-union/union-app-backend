package com.union.union.domain.auth.validator;

import com.union.union.domain.auth.exception.InvalidEmailFormatException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailFormatValidator {

    private static final String BASIC_EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String UNIVERSITY_EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)?(ac\\.kr|edu)$";

    public void validate(String email) {
        if (email == null || email.isBlank() || !Pattern.matches(BASIC_EMAIL_REGEX, email)) {
            throw new InvalidEmailFormatException("올바른 이메일 형식이 아닙니다");
        }

        if (!Pattern.matches(UNIVERSITY_EMAIL_REGEX, email)) {
            throw new InvalidEmailFormatException("대학교 이메일(.ac.kr 또는 .edu)만 사용할 수 있습니다");
        }
    }
}
