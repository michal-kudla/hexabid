package com.github.hexabid.adapter.in.authz.rest;

import com.github.hexabid.adapter.in.authz.dev.DevUserCatalog;
import com.github.hexabid.adapter.in.authz.dev.DevUserEntry;
import com.github.hexabid.adapter.in.authz.jwt.JwtTokenUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint do uzyskania JWT tokena (dev/local).
 * <p>
 * Użytkownik wybiera konto dev i otrzymuje JWT token.
 * Token jest używany w headerze "Authorization: Bearer <token>".
 */
@RestController
@Profile("local")
public class DevTokenController {

    private final DevUserCatalog devUserCatalog;
    private final JwtTokenUtil jwtTokenUtil;

    public DevTokenController(DevUserCatalog devUserCatalog, JwtTokenUtil jwtTokenUtil) {
        this.devUserCatalog = devUserCatalog;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Zwraca JWT token dla dev użytkownika.
     * GET /api/authz/token/{username}
     */
    @GetMapping("/api/authz/token/{username}")
    public ResponseEntity<Map<String, String>> getToken(@PathVariable String username) {
        DevUserEntry user = devUserCatalog.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String token = jwtTokenUtil.generateToken(
                "dev:" + user.getUsername(),
                user.getDisplayName(),
                user.toJwtRoles(),
                user.getOrganisationCode()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "roles", user.getRoles().toString(),
                "organisationCode", user.getOrganisationCode()
        ));
    }

    /**
     * Lista dostępnych dev użytkowników.
     * GET /api/authz/users
     */
    @GetMapping("/api/authz/users")
    public ResponseEntity<Map<String, Object>> listUsers() {
        var users = devUserCatalog.users().stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "displayName", u.getDisplayName(),
                        "roles", u.getRoles(),
                        "organisationCode", u.getOrganisationCode()
                ))
                .toList();

        return ResponseEntity.ok(Map.of("users", users));
    }
}
