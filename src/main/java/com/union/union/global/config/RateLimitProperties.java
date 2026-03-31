package com.union.union.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private final Limit login = new Limit(10, 60);
    private final Limit signup = new Limit(5, 60);
    private final Limit emailSend = new Limit(3, 60);
    private final Limit emailVerify = new Limit(5, 60);

    @Getter
    @Setter
    public static class Limit {
        private int maxRequests;
        private int windowSeconds;

        public Limit() {}

        public Limit(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
}
