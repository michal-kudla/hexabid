package com.github.hexabid.adapter.in.authz.dev;

import java.util.List;
import java.util.Objects;

/**
 * Katalog dev użytkowników.
 * Ładowany z application-local.yaml.
 */
public class DevUserCatalog {
    private final List<DevUserEntry> users;

    public DevUserCatalog(List<DevUserEntry> users) {
        this.users = users != null ? List.copyOf(users) : List.of();
    }

    public List<DevUserEntry> users() {
        return users;
    }

    public DevUserEntry findByUsername(String username) {
        Objects.requireNonNull(username, "username must not be null");
        return users.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElse(null);
    }
}
