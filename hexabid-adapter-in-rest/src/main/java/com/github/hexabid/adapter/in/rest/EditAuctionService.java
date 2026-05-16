package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.adapter.in.authz.principal.SpringAuthenticationPrincipalContextProvider;
import com.github.hexabid.authorization.core.context.model.AuthorizationContext;
import com.github.hexabid.authorization.core.permission.model.Action;
import com.github.hexabid.authorization.core.permission.model.Relation;
import com.github.hexabid.authorization.core.permission.model.ResourceType;
import com.github.hexabid.authorization.core.scope.model.OrganisationCode;
import com.github.hexabid.authorization.core.scope.port.out.SubordinateQueryPort;
import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.port.in.EditAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.EditAuctionResult;
import com.github.hexabid.core.auctioning.port.in.EditAuctionUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.auctioning.usecase.AuctionViews;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * Implementacja edycji aukcji w adapterze REST.
 * Autoryzacja oparta na AuthorizationContext z hexabid-authorization-core:
 * - OWN: createdByUserId == userId
 * - DIRECT_SUBORDINATE: SubordinateQueryPort
 * - ORG_SUBTREE: OrganisationCode.isSameOrBelow()
 * - ALL: Relation.ALL (AUCTION_ADMIN)
 */
@Service
public final class EditAuctionService implements EditAuctionUseCase {

    private static final Logger log = LoggerFactory.getLogger(EditAuctionService.class);

    private final AuctionRepository auctionRepository;
    private final SpringAuthenticationPrincipalContextProvider authContextProvider;
    private final @Nullable SubordinateQueryPort subordinateQueryPort;

    public EditAuctionService(AuctionRepository auctionRepository,
                               SpringAuthenticationPrincipalContextProvider authContextProvider,
                               @Nullable SubordinateQueryPort subordinateQueryPort) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository);
        this.authContextProvider = Objects.requireNonNull(authContextProvider);
        this.subordinateQueryPort = subordinateQueryPort;
    }

    @Override
    public EditAuctionResult editAuction(EditAuctionCommand command) {
        AuthorizationContext auth;
        try {
            auth = authContextProvider.currentAuthorizationContext();
        } catch (IllegalStateException e) {
            log.warn("AUTHZ DENY userId=anonymous resourceId={} action=EDIT reason=NOT_AUTHENTICATED",
                    command.auctionId());
            return new EditAuctionResult.EditNotAllowed("Not authenticated");
        }

        var auctionOpt = auctionRepository.findById(command.auctionId());
        if (auctionOpt.isEmpty()) {
            log.info("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NOT_FOUND",
                    auth.principal().userId(), command.auctionId());
            return new EditAuctionResult.EditNotAllowed("Auction not accessible");
        }

        Auction auction = auctionOpt.get();
        Relation matchedRelation = resolveRelation(auth, auction);

        if (!isAuthorized(auth, auction, matchedRelation)) {
            log.warn("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NO_PERMISSION roles={}",
                    auth.principal().userId(), command.auctionId(), auth.principal().roles());
            return new EditAuctionResult.EditNotAllowed("Insufficient permissions");
        }

        if (auction.status() != AuctionStatus.DRAFT) {
            log.info("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NOT_DRAFT status={}",
                    auth.principal().userId(), command.auctionId(), auction.status());
            return new EditAuctionResult.EditNotAllowed("Only draft auctions can be edited");
        }

        Auction edited = auction.edit(command.title(), command.startingPrice());
        Auction saved = auctionRepository.save(edited);

        log.info("AUTHZ ALLOW userId={} resourceId={} action=EDIT relation={}",
                auth.principal().userId(), command.auctionId(), matchedRelation);
        return new EditAuctionResult.AuctionEdited(AuctionViews.from(saved));
    }

    private Relation resolveRelation(AuthorizationContext auth, Auction auction) {
        String userId = auth.principal().userId();

        if (auction.createdByUserId().equals(userId)) {
            return Relation.OWN;
        }

        if (auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.ALL)) {
            return Relation.ALL;
        }

        if (subordinateQueryPort != null
                && subordinateQueryPort.isDirectSubordinate(userId, auction.createdByUserId())
                && auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE)) {
            return Relation.DIRECT_SUBORDINATE;
        }

        OrganisationCode userOrg = auth.principal().organisationCode();
        OrganisationCode resourceOrg = new OrganisationCode(auction.createdByOrganisationCode());
        if (userOrg.isSameOrBelow(resourceOrg)
                && auth.hasPermission(ResourceType.AUCTION, Action.EDIT, Relation.ORG_SUBTREE)) {
            return Relation.ORG_SUBTREE;
        }

        return null;
    }

    private boolean isAuthorized(AuthorizationContext auth, Auction auction, Relation matchedRelation) {
        if (matchedRelation == null) {
            return false;
        }
        return auth.hasPermission(ResourceType.AUCTION, Action.EDIT, matchedRelation);
    }
}
