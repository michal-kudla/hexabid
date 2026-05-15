package com.github.hexabid.adapter.in.authz.dev;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Definicja dev użytkownika z rolami i organisationCode.
 * Konfiguracja z application-local.yaml.
 */
public class DevUserEntry {
    private String username;
    private String password;
    private List<String> roles;
    private String organisationCode;
    private String displayName;

    public DevUserEntry() {
    }

    public DevUserEntry(String username, String password, List<String> roles, String organisationCode, String displayName) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.organisationCode = organisationCode;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getOrganisationCode() {
        return organisationCode;
    }

    public void setOrganisationCode(String organisationCode) {
        this.organisationCode = organisationCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Konwertuje rolę na format JWT claims: {role: "AUCTION_AUTHOR"}
     */
    public List<Map<String, String>> toJwtRoles() {
        Objects.requireNonNull(roles, "roles must not be null");
        return roles.stream()
                .map(role -> Map.of("role", role))
                .toList();
    }
}
