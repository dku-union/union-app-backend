package com.union.union.global.security.config;

import com.union.union.global.security.apikey.ApiKeyAuthenticationFilter;
import com.union.union.global.security.jwt.JwtAuthenticationFilter;
import com.union.union.global.security.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final Environment environment;

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/dev/**").permitAll()
                .requestMatchers("/auth/email/**").permitAll()
                .requestMatchers("/api/v1/universities/**").permitAll()
                // Publisher 로그인은 permitAll, /logout만 인증 필요
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/publisher/email/send").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/publisher/email/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/publisher/refresh").permitAll()

                // Analytics endpoints
                // POST /events: iOS/Android 네이티브 앱 (User JWT, ROLE_USER)
                .requestMatchers(HttpMethod.POST, "/api/v1/analytics/events").authenticated()
                // GET summary/events: 퍼블리셔 대시보드 (@PreAuthorize 로 세부 인가)
                .requestMatchers(HttpMethod.GET, "/api/v1/analytics/**").authenticated()

                // JWKS (Public) — publisher 백엔드가 미니앱 ID 토큰 검증용 공개키 조회
                .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()

                // Banner endpoints (Public) — Home 캐러셀 노출용
                .requestMatchers(HttpMethod.GET, "/banners").permitAll()

                // MiniApp endpoints (Public)
                .requestMatchers(HttpMethod.GET, "/mini-apps/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/mini-apps").permitAll()
                .requestMatchers(HttpMethod.GET, "/mini-apps/popular").permitAll()
                .requestMatchers(HttpMethod.GET, "/mini-apps/discovery").permitAll()
                .requestMatchers(HttpMethod.GET, "/mini-apps/search/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/mini-apps/search/click").permitAll()
                .requestMatchers(HttpMethod.GET, "/mini-apps/category/**").permitAll()

                // MiniApp endpoints (Authenticated)
                .requestMatchers(HttpMethod.GET, "/mini-apps/recommendations").authenticated()
                .requestMatchers(HttpMethod.POST, "/mini-apps/*/launch").authenticated()
                .requestMatchers(HttpMethod.POST, "/mini-apps").hasRole("PUBLISHER")
                .requestMatchers(HttpMethod.PATCH, "/mini-apps/*/approve").hasRole("ADMIN")

                // Publisher endpoints
                // TODO: Publisher apply/approve는 NextAuth 기반으로 전환됨
                // .requestMatchers(HttpMethod.POST, "/publishers/apply").authenticated()
                // .requestMatchers(HttpMethod.PATCH, "/publishers/*/approve").hasRole("ADMIN")

                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Notification
                .requestMatchers("/notifications/token").authenticated()
                .requestMatchers("/notifications/inbox/**").authenticated()
                .requestMatchers("/notifications/inbox").authenticated()
                .requestMatchers("/notifications/unread-count").authenticated()

                // Publisher 알림 발송 (API Key 인증)
                .requestMatchers(HttpMethod.POST, "/api/v1/publishers/notifications")
                    .hasAuthority("SCOPE_notifications:send")

                // Publisher 알림 발송 (대시보드 JWT 인증, 테스트용)
                .requestMatchers(HttpMethod.POST, "/api/v1/publishers/me/notifications")
                    .hasRole("PUBLISHER")

                // Publisher API Key 관리 (JWT 인증, PUBLISHER 권한)
                .requestMatchers("/api/v1/publishers/me/api-keys/**").hasRole("PUBLISHER")
                .requestMatchers("/api/v1/publishers/me/api-keys").hasRole("PUBLISHER")

                // 사용자 미니앱 구독 (JWT 인증)
                .requestMatchers("/api/v1/users/me/miniapps/**").authenticated()
                .requestMatchers("/api/v1/users/me/subscriptions").authenticated()

                // Report
                .requestMatchers(HttpMethod.POST, "/reports").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(apiKeyAuthenticationFilter, RateLimitFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, ApiKeyAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        boolean isLocal = Arrays.asList(environment.getActiveProfiles()).contains("local");

        if (isLocal || allowedOrigins == null || allowedOrigins.isBlank()) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
            config.setAllowCredentials(true);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
