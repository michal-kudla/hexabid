package com.github.hexabid.auth.core.identityaccess.model;

import com.github.hexabid.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record AuthenticatedUser(
        PartyId partyId,
        String provider,
        String subject,
        String displayName,
        @Nullable String email,
        java.util.Set<String> roles
) {

    public AuthenticatedUser {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        roles = roles != null ? java.util.Set.copyOf(roles) : java.util.Set.of();
    }

    public AuthenticatedUser(PartyId partyId, String provider, String subject, String displayName, @Nullable String email) {
        this(partyId, provider, subject, displayName, email, java.util.Set.of());
    }
}
