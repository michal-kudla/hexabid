package com.github.hexabid.auth.core.identityaccess.port.in;

import java.util.Optional;

public interface FindCurrentUserProfileUseCase {
    Optional<CurrentUserProfileView> findCurrentUserProfile();
}
