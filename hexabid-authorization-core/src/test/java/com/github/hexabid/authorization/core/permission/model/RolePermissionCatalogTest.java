package com.github.hexabid.authorization.core.permission.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionCatalogTest {

    @Test
    void authorRoleMapsToOwnCreateReadEdit() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_AUTHOR");

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.CREATE, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.OWN)));
    }

    @Test
    void managerRoleMapsToSubordinateAndOrgRead() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_MANAGER");

        assertEquals(7, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.CREATE, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.DIRECT_SUBORDINATE)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.APPROVE, Relation.DIRECT_SUBORDINATE)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE)));
    }

    @Test
    void adminRoleMapsToAllActionsAndRelations() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_ADMIN");

        assertEquals(5, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.CREATE, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.DELETE, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.APPROVE, Relation.ALL)));
    }

    @Test
    void reportViewerMapsToOrgSubtreeRead() {
        var permissions = RolePermissionCatalog.permissionsFor("REPORT_VIEWER");

        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.REPORT, Action.READ, Relation.ORG_SUBTREE)));
    }

    @Test
    void unknownRoleMapsToEmptyPermissions() {
        var permissions = RolePermissionCatalog.permissionsFor("UNKNOWN_ROLE");
        assertTrue(permissions.isEmpty());
    }
}
