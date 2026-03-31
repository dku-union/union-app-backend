package com.union.union.domain.appversion.dto;

import java.util.UUID;

public record CreateVersionResponseDto(
    UUID versionId,
    String uploadUrl
) {
}
