package com.github.hexabid.authorization.core.context.model;

import com.github.hexabid.authorization.core.permission.model.Action;
import com.github.hexabid.authorization.core.permission.model.Permission;
import com.github.hexabid.authorization.core.permission.model.Relation;
import com.github.hexabid.authorization.core.permission.model.ResourceType;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import com.github.hexabid.authorization.core.scope.model.OrganisationCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationContextTest {

    @Test
    void hasPermissionReturnsTrueForMatchingPermission() {
        var auth = createContext(
                new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)
        );

        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.READ, Relation.OWN));
    }

    @Test
    void hasPermissionReturnsFalseForMissingPermission() {
        var auth = createContext(
                new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)
        );

        assertFalse(auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.OWN));
        assertFalse(auth.hasPermission(ResourceType.REPORT, Action.READ, Relation.OWN));
    }

    @Test
    void hasAnyPermissionReturnsTrueWhenAnyRelationMatches() {
        var auth = createContext(
                new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN),
                new Permission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE)
        );

        assertTrue(auth.hasAnyPermission(ResourceType.AUCTION, Action.READ));
    }

    @Test
    void hasAnyPermissionReturnsFalseWhenNoMatch() {
        var auth = createContext(
                new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)
        );

        assertFalse(auth.hasAnyPermission(ResourceType.AUCTION, Action.EDIT));
        assertFalse(auth.hasAnyPermission(ResourceType.REPORT, Action.READ));
    }

    @Test
    void rejectsNullPrincipal() {
        assertThrows(NullPointerException.class, () ->
                new AuthorizationContext(null, Set.of())
        );
    }

    @Test
    void rejectsNullPermissions() {
        var principal = new PrincipalContext("user-1", Set.of("AUCTION_AUTHOR"), new OrganisationCode("A12"));
        assertThrows(NullPointerException.class, () ->
                new AuthorizationContext(principal, null)
        );
    }

    private AuthorizationContext createContext(Permission... permissions) {
        var principal = new PrincipalContext(
                "user-1",
                Set.of("AUCTION_AUTHOR"),
                new OrganisationCode("A12/B04/C77")
        );
        return new AuthorizationContext(principal, Set.of(permissions));
    }
}
