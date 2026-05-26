package com.union.union.domain.subscription.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePushEnabledRequestDto(
        @NotNull Boolean pushEnabled
) {}
