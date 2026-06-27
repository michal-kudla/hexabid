package com.github.hexabid.authorization.core.context.model;

import com.github.hexabid.authorization.core.permission.model.Action;
import com.github.hexabid.authorization.core.permission.model.Permission;
import com.github.hexabid.authorization.core.permission.model.Relation;
import com.github.hexabid.authorization.core.permission.model.ResourceType;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;

import java.util.Objects;
import java.util.Set;

/**
 * Kontekst autoryzacji łączy PrincipalContext z wyliczonymi uprawnieniami.
 * Używany do sprawdzania dostępu do zasobów.
 *
 * @param principal   kontekst uwierzytelnionego użytkownika
 * @param permissions zestaw uprawnień wyliczony z ról
 */
public record AuthorizationContext(
        PrincipalContext principal,
        Set<Permission> permissions
) {

    public AuthorizationContext {
        Objects.requireNonNull(principal, "principal must not be null");
        Objects.requireNonNull(permissions, "permissions must not be null");
    }

    /**
     * Sprawdza, czy kontekst zawiera konkretne uprawnienie.
     */
    public boolean hasPermission(ResourceType resourceType, Action action, Relation relation) {
        return permissions.contains(new Permission(resourceType, action, relation));
    }

    /**
     * Sprawdza, czy kontekst zawiera jakiekolwiek uprawnienie dla danego typu zasobu i akcji
     * (bez względu na relację). Używane jako wstępny guard przed authorized query.
     */
    public boolean hasAnyPermission(ResourceType resourceType, Action action) {
        return permissions.stream()
                .anyMatch(p -> p.resourceType() == resourceType && p.action() == action);
    }
}
