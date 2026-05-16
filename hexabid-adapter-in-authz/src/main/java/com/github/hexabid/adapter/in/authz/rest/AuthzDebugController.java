package com.github.hexabid.adapter.in.authz.rest;

import com.github.hexabid.adapter.in.authz.principal.SpringAuthenticationPrincipalContextProvider;
import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.context.usecase.AuthorizationContextFactory;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Endpoint diagnostyczny -- zwraca bieżący kontekst autoryzacji.
 * Tylko dla dev/local.
 */
@RestController
@Profile("local")
public class AuthzDebugController {

    private final SpringAuthenticationPrincipalContextProvider contextProvider;

    public AuthzDebugController(SpringAuthenticationPrincipalContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @GetMapping("/api/authz/me")
    public Map<String, Object> me() {
        PrincipalContext principal = contextProvider.current();
        AuthorizationContext auth = AuthorizationContextFactory.create(principal);

        return Map.of(
                "userId", principal.userId(),
                "roles", principal.roles(),
                "organisationCode", principal.organisationCode().value(),
                "effectivePermissions", auth.permissions().stream()
                        .map(p -> p.resourceType() + ":" + p.action() + ":" + p.relation())
                        .collect(Collectors.toSet())
        );
    }
}
