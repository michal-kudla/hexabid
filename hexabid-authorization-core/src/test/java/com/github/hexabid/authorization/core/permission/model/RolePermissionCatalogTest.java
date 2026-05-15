package com.github.hexabid.authorization.core.permission.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionCatalogTest {

    @Test
    void authorRoleMapsToOwnReadAndEdit() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_AUTHOR");

        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.OWN)));
    }

    @Test
    void managerRoleMapsToDirectSubordinateAndOrgRead() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_MANAGER");

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.DIRECT_SUBORDINATE)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE)));
    }

    @Test
    void adminRoleMapsToAllReadEditDelete() {
        var permissions = RolePermissionCatalog.permissionsFor("AUCTION_ADMIN");

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.READ, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.EDIT, Relation.ALL)));
        assertTrue(permissions.contains(new Permission(ResourceType.AUCTION, Action.DELETE, Relation.ALL)));
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
