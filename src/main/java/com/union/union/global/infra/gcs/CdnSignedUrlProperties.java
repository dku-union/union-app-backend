package com.union.union.global.infra.gcs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cdn")
public record CdnSignedUrlProperties(
    String baseUrl,
    String keyName,
    String keyValue
) {
}
