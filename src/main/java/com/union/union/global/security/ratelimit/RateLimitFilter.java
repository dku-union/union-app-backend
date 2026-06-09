package com.union.union.global.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.union.union.global.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        RateLimitProperties.Limit limit = resolveLimit(path, method);
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = RateLimitService.keyOf(clientIp, path);

        if (!rateLimitService.isAllowed(key, limit.getMaxRequests(), limit.getWindowSeconds())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
                    "timestamp", LocalDateTime.now().toString()
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitProperties.Limit resolveLimit(String path, String method) {
        if (!"POST".equalsIgnoreCase(method)) {
            return null;
        }

        if (path.equals("/api/v1/auth/login")) {
            return properties.getLogin();
        }
        if (path.equals("/api/v1/auth/signup")) {
            return properties.getSignup();
        }
        if (path.equals("/auth/email/send")) {
            return properties.getEmailSend();
        }
        if (path.equals("/auth/email/verify")) {
            return properties.getEmailVerify();
        }
        if (path.equals("/api/v1/auth/publisher/email/send")) {
            return properties.getPublisherEmailSend();
        }
        if (path.equals("/api/v1/auth/publisher/email/verify")) {
            return properties.getPublisherEmailVerify();
        }

        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
