package com.github.hexabid.auth.core.identityaccess.port.out;

import com.github.hexabid.auth.core.identityaccess.model.AuthenticatedUser;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<AuthenticatedUser> maybeCurrentUser();
}
