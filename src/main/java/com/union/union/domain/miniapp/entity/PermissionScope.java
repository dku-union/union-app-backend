package com.union.union.domain.miniapp.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Objects;

/**
 * 미니앱이 요청할 수 있는 권한 스코프 (정본).
 * SDK(union.config.json) · iOS · 대시보드와 동일한 닷-표기 7개로 통일된 계약이다.
 */
public enum PermissionScope {
    USER_PROFILE("user.profile"),
    USER_EMAIL("user.email"),
    USER_UNIVERSITY("user.university"),
    DEVICE_LOCATION("device.location"),
    DEVICE_CAMERA("device.camera"),
    DEVICE_STORAGE("device.storage"),
    NOTIFICATION("notification");

    private final String value;

    PermissionScope(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 알 수 없는 값은 예외 대신 {@code null} 을 반환하는 관대 파서.
     * 레거시 JSONB row(payment / share / user.student_info / location / camera 등)가
     * 엔티티 로드 시 역직렬화 단계에서 500 을 내지 않도록 하기 위함이다.
     * 호출부는 {@link #sanitize(List)} 로 null 원소를 제거해 사용한다.
     */
    @JsonCreator
    public static PermissionScope fromNullable(String value) {
        if (value == null) return null;
        for (PermissionScope scope : values()) {
            if (scope.value.equals(value)) return scope;
        }
        return null;
    }

    /** null / 미지 스코프를 제거한 안전한 리스트를 반환한다. */
    public static List<PermissionScope> sanitize(List<PermissionScope> scopes) {
        if (scopes == null) return List.of();
        return scopes.stream().filter(Objects::nonNull).toList();
    }
}
