package com.union.union.domain.miniapp.dto;

import jakarta.validation.constraints.Size;

public record UpdateMiniAppRequestDto(
        @Size(min = 2, max = 100) String name,
        @Size(max = 2000) String description
) {
}
