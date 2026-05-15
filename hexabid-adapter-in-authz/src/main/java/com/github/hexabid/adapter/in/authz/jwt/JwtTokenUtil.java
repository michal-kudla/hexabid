package com.github.hexabid.adapter.in.authz.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility do generowania i parsowania JWT tokenów.
 * Używa HMAC-SHA256 z kluczem z konfiguracji.
 */
public final class JwtTokenUtil {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtTokenUtil(String secret, long expirationHours) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 3600 * 1000;
    }

    /**
     * Generuje JWT token z claims: sub, roles, organisationCode.
     */
    public String generateToken(String subject, String displayName, List<Map<String, String>> roles,
                                 String organisationCode) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(subject)
                .claim("displayName", displayName)
                .claim("roles", roles)
                .claim("organisationCode", organisationCode)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parsuje JWT token i zwraca Claims.
     */
    public io.jsonwebtoken.Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
