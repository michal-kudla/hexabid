package com.github.hexabid.auth.core.identityaccess.port.in;

import org.jspecify.annotations.Nullable;

public record CurrentUserProfileView(
        String partyId,
        String provider,
        String displayName,
        @Nullable String email,
        boolean verified
) {
}
