package com.union.union.global.common.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", message);
    }
}
