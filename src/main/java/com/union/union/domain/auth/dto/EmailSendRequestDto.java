package com.union.union.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailSendRequestDto(
    @JsonProperty("email") String email,
    @JsonProperty("universityId") Long universityId
) {}
