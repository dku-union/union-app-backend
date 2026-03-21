package com.union.union.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailVerifyRequestDto(
    @JsonProperty("email") String email,
    @JsonProperty("code") String code
) {}
