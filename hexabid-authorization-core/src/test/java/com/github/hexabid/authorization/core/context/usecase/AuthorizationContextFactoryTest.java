package com.github.hexabid.authorization.core.context.usecase;

import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.permission.model.Action;
import com.github.hexabid.authorization.core.permission.model.Permission;
import com.github.hexabid.authorization.core.permission.model.Relation;
import com.github.hexabid.authorization.core.permission.model.ResourceType;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import com.github.hexabid.authorization.core.scope.model.OrganisationCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationContextFactoryTest {

    @Test
    void createsContextWithPermissionsFromRoles() {
        var principal = new PrincipalContext(
                "user-1",
                Set.of("AUCTION_AUTHOR"),
                new OrganisationCode("A12/B04/C77")
        );

        var auth = AuthorizationContextFactory.create(principal);

        assertEquals(principal, auth.principal());
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.READ, Relation.OWN));
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.OWN));
        assertFalse(auth.hasPermission(ResourceType.AUCTION, Action.DELETE, Relation.OWN));
    }

    @Test
    void mergesPermissionsFromMultipleRoles() {
        var principal = new PrincipalContext(
                "user-1",
                Set.of("AUCTION_AUTHOR", "AUCTION_MANAGER"),
                new OrganisationCode("A12/B04")
        );

        var auth = AuthorizationContextFactory.create(principal);

        // AUCTION_AUTHOR permissions
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.READ, Relation.OWN));
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.OWN));

        // AUCTION_MANAGER permissions
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.READ, Relation.DIRECT_SUBORDINATE));
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE));
        assertTrue(auth.hasPermission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE));
    }

    @Test
    void unknownRoleProducesNoPermissions() {
        var principal = new PrincipalContext(
                "user-1",
                Set.of("UNKNOWN_ROLE"),
                new OrganisationCode("A12")
        );

        var auth = AuthorizationContextFactory.create(principal);

        assertTrue(auth.permissions().isEmpty());
        assertFalse(auth.hasAnyPermission(ResourceType.AUCTION, Action.READ));
    }

    @Test
    void rejectsNullPrincipal() {
        assertThrows(NullPointerException.class, () ->
                AuthorizationContextFactory.create(null)
        );
    }
}
