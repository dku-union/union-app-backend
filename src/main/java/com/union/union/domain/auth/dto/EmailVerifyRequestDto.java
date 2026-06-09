package com.union.union.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequestDto(
    @NotBlank @JsonProperty("email") String email,
    @NotBlank @JsonProperty("code") String code
) {}
