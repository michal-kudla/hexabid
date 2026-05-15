package com.github.hexabid.authorization.core.context.usecase;

import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.permission.model.Permission;
import com.github.hexabid.authorization.core.permission.model.RolePermissionCatalog;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fabryka kontekstu autoryzacji.
 * Łączy PrincipalContext z wyliczonymi Permission na podstawie RolePermissionCatalog.
 */
public final class AuthorizationContextFactory {

    private AuthorizationContextFactory() {
    }

    /**
     * Tworzy AuthorizationContext z PrincipalContext.
     * Uprawnienia są wyliczane z ról użytkownika.
     */
    public static AuthorizationContext create(PrincipalContext principal) {
        Objects.requireNonNull(principal, "principal must not be null");

        Set<Permission> permissions = principal.roles().stream()
                .flatMap(role -> RolePermissionCatalog.permissionsFor(role).stream())
                .collect(Collectors.toUnmodifiableSet());

        return new AuthorizationContext(principal, permissions);
    }
}
