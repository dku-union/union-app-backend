package com.union.union.domain.auth.exception;

import com.union.union.global.common.exception.BadRequestException;

public class InvalidUniversityDomainException extends BadRequestException {

    public InvalidUniversityDomainException(String message) {
        super(message);
    }
}
