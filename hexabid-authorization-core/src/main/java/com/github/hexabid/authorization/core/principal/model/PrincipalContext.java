package com.github.hexabid.authorization.core.principal.model;

import com.github.hexabid.authorization.core.scope.model.OrganisationCode;

import java.util.Objects;
import java.util.Set;

/**
 * Kontekst uwierzytelnionego użytkownika.
 * Powstaje z JWT po walidacji przez Spring Security.
 *
 * @param userId           identyfikator użytkownika (JWT sub)
 * @param roles            role biznesowe (JWT roles)
 * @param organisationCode zakodowana pozycja w hierarchii organizacyjnej
 */
public record PrincipalContext(
        String userId,
        Set<String> roles,
        OrganisationCode organisationCode
) {
    public PrincipalContext {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        Objects.requireNonNull(organisationCode, "organisationCode must not be null");
    }
}
