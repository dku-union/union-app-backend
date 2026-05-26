package com.union.union.global.security.apikey;

import java.util.UUID;

public record PublisherApiKeyPrincipal(
        UUID publisherId,
        Long apiKeyId
) {}
