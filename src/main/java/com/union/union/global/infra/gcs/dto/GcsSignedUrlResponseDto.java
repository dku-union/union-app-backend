package com.union.union.global.infra.gcs.dto;

public record GcsSignedUrlResponseDto(
    String signedUrl,
    String fileUrl
) {
}
