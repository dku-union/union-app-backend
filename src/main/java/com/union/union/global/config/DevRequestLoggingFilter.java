package com.union.union.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Component
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DevRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // multipart 요청은 wrapper 로 감싸지 않고 그대로 통과시킨다.
        // ReReadableRequestWrapper.getInputStream() 이 ByteArrayInputStream 을 반환하면
        // Tomcat 의 multipart parser 가 boundary/parts 를 정상적으로 분리하지 못해
        // @RequestPart 가 MissingServletRequestPartException 으로 실패한다.
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            long start = System.currentTimeMillis();
            try {
                filterChain.doFilter(request, wrappedResponse);
            } finally {
                long duration = System.currentTimeMillis() - start;
                logRequest(request, wrappedResponse, new byte[0], duration);
                wrappedResponse.copyBodyToResponse();
            }
            return;
        }

        // body를 미리 읽어 두고 다운스트림에서도 재사용 가능한 wrapper
        byte[] bodyBytes = request.getInputStream().readAllBytes();
        HttpServletRequest reReadableRequest = new ReReadableRequestWrapper(request, bodyBytes);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(reReadableRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;
            logRequest(reReadableRequest, wrappedResponse, bodyBytes, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(
            HttpServletRequest request,
            ContentCachingResponseWrapper response,
            byte[] bodyBytes,
            long duration
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        int status = response.getStatus();

        StringBuilder sb = new StringBuilder();
        sb.append("\n┌──────────────── DEV REQUEST LOG ────────────────");
        sb.append("\n│ ").append(method).append(" ").append(uri);
        if (query != null) sb.append("?").append(query);
        sb.append("  →  ").append(status).append(" (").append(duration).append("ms)");

        sb.append("\n│ Remote: ").append(request.getRemoteAddr());

        sb.append("\n│ Headers:");
        Collections.list(request.getHeaderNames()).forEach(name -> {
            String value = name.equalsIgnoreCase("authorization")
                    ? maskAuthorization(request.getHeader(name))
                    : request.getHeader(name);
            sb.append("\n│   ").append(name).append(": ").append(value);
        });

        if (bodyBytes.length > 0) {
            String bodyStr = compact(new String(bodyBytes, StandardCharsets.UTF_8), 300);
            sb.append("\n│ Body: ").append(bodyStr);
        }

        byte[] resBody = response.getContentAsByteArray();
        if (resBody.length > 0) {
            String resStr = compact(new String(resBody, StandardCharsets.UTF_8), 200);
            sb.append("\n│ Response: ").append(resStr);
        }

        sb.append("\n└─────────────────────────────────────────────────");

        if (status >= 400) {
            log.warn(sb.toString());
        } else {
            log.info(sb.toString());
        }
    }

    private String compact(String text, int maxLen) {
        String s = text.replaceAll("\\s+", " ").trim();
        return s.length() > maxLen ? s.substring(0, maxLen) + "…" : s;
    }

    private String maskAuthorization(String value) {
        if (value == null || value.length() <= 20) return "***";
        return value.substring(0, 15) + "...***";
    }

    /** 미리 읽은 body bytes를 getInputStream()으로 반복 제공하는 wrapper */
    private static class ReReadableRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        ReReadableRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return bais.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {}
                @Override public int read() { return bais.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
