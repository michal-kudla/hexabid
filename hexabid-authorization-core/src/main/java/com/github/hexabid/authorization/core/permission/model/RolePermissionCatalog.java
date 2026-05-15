package com.github.hexabid.authorization.core.permission.model;

import java.util.Map;
import java.util.Set;

/**
 * Katalog mapowania ról biznesowych na zestaw uprawnień.
 * Role są definiowane w JWT, a aplikacja wylicza z nich Permission.
 * <p>
 * Role biznesowe (np. AUCTION_AUTHOR, AUCTION_MANAGER) są mapowane na
 * Permission(ResourceType, Action, Relation) -- unikamy flat permission names.
 */
public final class RolePermissionCatalog {

    private RolePermissionCatalog() {
    }

    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            "AUCTION_AUTHOR", Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.OWN)
            ),
            "AUCTION_MANAGER", Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.DIRECT_SUBORDINATE),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE),
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE)
            ),
            "AUCTION_ADMIN", Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.ALL),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.ALL),
                    new Permission(ResourceType.AUCTION, Action.DELETE, Relation.ALL)
            ),
            "REPORT_VIEWER", Set.of(
                    new Permission(ResourceType.REPORT, Action.READ, Relation.ORG_SUBTREE)
            )
    );

    /**
     * Zwraca zestaw uprawnień dla podanej roli.
     * Jeśli rola jest nieznana, zwraca pusty zestaw.
     */
    public static Set<Permission> permissionsFor(String role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Set.of());
    }
}
