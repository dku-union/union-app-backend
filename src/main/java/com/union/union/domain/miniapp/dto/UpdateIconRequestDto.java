package com.union.union.domain.miniapp.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateIconRequestDto(
        @NotBlank String iconUrl
) {
}
