package com.github.hexabid.adapter.in.authz.principal;

import com.github.hexabid.authorization.core.principal.model.PrincipalContext;
import com.github.hexabid.authorization.core.scope.model.OrganisationCode;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapuje JWT Claims na PrincipalContext.
 */
@Component
public class JwtPrincipalContextMapper {

    /**
     * Tworzy PrincipalContext z JWT Claims.
     */
    @SuppressWarnings("unchecked")
    public PrincipalContext map(Claims claims) {
        String userId = claims.getSubject();
        String organisationCode = claims.get("organisationCode", String.class);

        List<Map<String, String>> rolesList = claims.get("roles", List.class);
        Set<String> roles = rolesList.stream()
                .map(roleMap -> ((Map<String, String>) roleMap).get("role"))
                .collect(Collectors.toUnmodifiableSet());

        return new PrincipalContext(
                userId,
                roles,
                new OrganisationCode(organisationCode)
        );
    }
}
