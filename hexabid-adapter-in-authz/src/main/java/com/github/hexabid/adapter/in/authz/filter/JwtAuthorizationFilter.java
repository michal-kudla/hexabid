package com.github.hexabid.adapter.in.authz.filter;

import com.github.hexabid.adapter.in.authz.jwt.JwtTokenUtil;
import com.github.hexabid.adapter.in.authz.principal.JwtPrincipalContextMapper;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Filtr JWT -- parsuje token z headera Authorization i ładuje PrincipalContext do SecurityContext.
 * <p>
 * Format headera: "Authorization: Bearer <token>"
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtPrincipalContextMapper principalContextMapper;

    public JwtAuthorizationFilter(JwtTokenUtil jwtTokenUtil, JwtPrincipalContextMapper principalContextMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.principalContextMapper = principalContextMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenUtil.parseToken(token);
                PrincipalContext principal = principalContextMapper.map(claims);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                logger.warn("Failed to parse JWT: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
