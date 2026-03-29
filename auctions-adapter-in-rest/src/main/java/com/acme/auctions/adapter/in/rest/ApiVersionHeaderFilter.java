package com.acme.auctions.adapter.in.rest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiVersionHeaderFilter extends OncePerRequestFilter {

    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String DEFAULT_VERSION = "1";

    private final String currentVersion;

    public ApiVersionHeaderFilter(@Value("${auctions.api.version.current:1}") String currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestedVersion = request.getHeader(API_VERSION_HEADER);
        String effectiveVersion = requestedVersion == null || requestedVersion.isBlank()
                ? DEFAULT_VERSION
                : requestedVersion;

        if (!currentVersion.equals(effectiveVersion)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"message":"Unsupported API version","expectedHeader":"X-API-Version","supportedVersion":"%s"}
                    """.formatted(currentVersion).trim());
            return;
        }

        response.setHeader(API_VERSION_HEADER, currentVersion);
        filterChain.doFilter(request, response);
    }
}
