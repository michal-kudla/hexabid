package com.github.hexabid.adapter.in.authz.principal;

import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.context.usecase.AuthorizationContextFactory;
import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Dostarcza PrincipalContext z bieżącej autentykacji Spring Security.
 */
@Component
public class SpringAuthenticationPrincipalContextProvider {

    /**
     * Pobiera PrincipalContext z SecurityContext.
     * Oczekuje, że Authentication.getPrincipal() zwraca PrincipalContext.
     */
    public PrincipalContext current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof PrincipalContext principal)) {
            throw new IllegalStateException("No PrincipalContext in SecurityContext");
        }
        return principal;
    }

    /**
     * Tworzy AuthorizationContext z bieżącej autentykacji.
     */
    public AuthorizationContext currentAuthorizationContext() {
        return AuthorizationContextFactory.create(current());
    }
}
