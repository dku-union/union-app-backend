package com.union.union.domain.miniapp.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PermissionScope {
    USER_PROFILE("user.profile"),
    USER_STUDENT_INFO("user.student_info"),
    PAYMENT("payment"),
    LOCATION("location"),
    NOTIFICATION("notification"),
    CAMERA("camera"),
    SHARE("share");

    private final String value;

    PermissionScope(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PermissionScope from(String value) {
        for (PermissionScope scope : values()) {
            if (scope.value.equals(value)) return scope;
        }
        throw new IllegalArgumentException("알 수 없는 권한 스코프입니다: " + value);
    }
}
