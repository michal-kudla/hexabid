package com.github.hexabid.adapter.in.authz.service;

import com.github.hexabid.adapter.in.authz.principal.SpringAuthenticationPrincipalContextProvider;
import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.permission.model.Action;
import com.github.hexabid.authorization.core.permission.model.ResourceType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Serwis autoryzacji -- bean dostępny w SpEL przez @PreAuthorize.
 * <p>
 * Użycie w kontrolerach:
 * <pre>
 *   @PreAuthorize("@authzService.canEditAuction(#auctionId)")
 * </pre>
 */
@Service("authzService")
public class AuthzService {

    private final SpringAuthenticationPrincipalContextProvider contextProvider;

    public AuthzService(SpringAuthenticationPrincipalContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    /**
     * Sprawdza, czy bieżący użytkownik może czytać aukcję.
     * Wstępny guard -- ostateczna decyzja przez authorized query w repository.
     */
    public boolean canReadAuction(String auctionId) {
        return hasAnyPermission(ResourceType.AUCTION, Action.READ);
    }

    /**
     * Sprawdza, czy bieżący użytkownik może edytować aukcję.
     * Wstępny guard -- ostateczna decyzja przez authorized query w repository.
     */
    public boolean canEditAuction(String auctionId) {
        return hasAnyPermission(ResourceType.AUCTION, Action.EDIT);
    }

    /**
     * Sprawdza, czy bieżący użytkownik może usuwać aukcję.
     */
    public boolean canDeleteAuction(String auctionId) {
        return hasAnyPermission(ResourceType.AUCTION, Action.DELETE);
    }

    /**
     * Sprawdza, czy bieżący użytkownik może aktywować aukcję.
     */
    public boolean canApproveAuction(String auctionId) {
        return hasAnyPermission(ResourceType.AUCTION, Action.APPROVE);
    }

    /**
     * Sprawdza, czy bieżący użytkownik może tworzyć aukcje.
     */
    public boolean canCreateAuction() {
        return hasAnyPermission(ResourceType.AUCTION, Action.CREATE);
    }

    /**
     * Sprawdza, czy bieżący użytkownik może czytać raporty.
     */
    public boolean canReadReports() {
        return hasAnyPermission(ResourceType.REPORT, Action.READ);
    }

    private boolean hasAnyPermission(ResourceType resourceType, Action action) {
        try {
            AuthorizationContext auth = contextProvider.currentAuthorizationContext();
            return auth.hasAnyPermission(resourceType, action);
        } catch (Exception e) {
            return false;
        }
    }
}
