package com.github.hexabid.adapter.in.auth.local;

import com.github.hexabid.auth.core.identityaccess.model.AuthenticatedUser;
import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.core.party.model.PartyId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LocalCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<AuthenticatedUser> maybeCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            PartyId partyId = new PartyId("local:" + username);
            return Optional.of(new AuthenticatedUser(partyId, "local", username, username, username + "@example.com"));
        }
        
        return Optional.empty();
    }
}
