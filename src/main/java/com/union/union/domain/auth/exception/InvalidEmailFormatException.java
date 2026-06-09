package com.union.union.domain.auth.exception;

import com.union.union.global.common.exception.BadRequestException;

public class InvalidEmailFormatException extends BadRequestException {

    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
