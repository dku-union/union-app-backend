package com.union.union.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequestDto(
        @NotBlank @Size(max = 50)
        String nickname
) {}
