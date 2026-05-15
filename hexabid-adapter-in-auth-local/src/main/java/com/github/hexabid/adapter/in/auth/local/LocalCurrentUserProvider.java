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
        if (principal instanceof com.github.hexabid.authorization.core.principal.model.PrincipalContext pc) {
            return Optional.of(new AuthenticatedUser(
                    new PartyId(pc.userId()), "jwt", pc.userId(), pc.userId(), null, pc.roles()));
        }
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            PartyId partyId = new PartyId("local:" + username);
            java.util.Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(java.util.stream.Collectors.toSet());
            return Optional.of(new AuthenticatedUser(partyId, "local", username, username, username + "@example.com", roles));
        }
        
        return Optional.empty();
    }
}
