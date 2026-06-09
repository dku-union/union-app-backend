package com.union.union.global.security.apikey;

import com.union.union.domain.publisher.entity.ApiKey;
import com.union.union.domain.publisher.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Union-Api-Key";
    public static final String ROLE_PUBLISHER_API_KEY = "ROLE_PUBLISHER_API_KEY";
    public static final String SCOPE_PREFIX = "SCOPE_";

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String raw = request.getHeader(HEADER);

        if (StringUtils.hasText(raw)) {
            Optional<ApiKey> verified = apiKeyService.verify(raw);
            if (verified.isPresent()) {
                ApiKey apiKey = verified.get();
                UUID publisherId = apiKey.getPublisher().getPublisherId();

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(ROLE_PUBLISHER_API_KEY));
                for (String scope : apiKey.getScopes().split(",")) {
                    String trimmed = scope.trim();
                    if (!trimmed.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority(SCOPE_PREFIX + trimmed));
                    }
                }

                PublisherApiKeyPrincipal principal = new PublisherApiKeyPrincipal(publisherId, apiKey.getId());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                apiKeyService.touchAsync(apiKey.getId(), extractIp(request));
                log.debug("API Key 인증 성공. publisherId={}, apiKeyId={}", publisherId, apiKey.getId());
            } else {
                log.debug("API Key 인증 실패. prefix={}", raw.length() > 12 ? raw.substring(0, 12) : raw);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
