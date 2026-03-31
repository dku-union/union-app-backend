package com.union.union.domain.publisher.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherApplyRequestDto(
    @NotBlank(message = "퍼블리셔 이름은 필수입니다")
    @Size(max = 100, message = "퍼블리셔 이름은 100자 이내여야 합니다")
    String name,

    String description,

    @NotBlank(message = "담당자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이내여야 합니다")
    String contactEmail
) {
}
