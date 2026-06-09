package com.union.union.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequestDto(
        @NotBlank
        String imageUrl
) {}
