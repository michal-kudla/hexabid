package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.adapter.out.db.SpringDataUserSupervisionRepository;
import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.port.in.EditAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.EditAuctionResult;
import com.github.hexabid.core.auctioning.port.in.EditAuctionUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.auctioning.usecase.AuctionViews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * Implementacja edycji aukcji w adapterze REST.
 * Autoryzacja na podstawie Permission (ResourceType, Action, Relation):
 * - OWN: createdByUserId == userId
 * - DIRECT_SUBORDINATE: user_supervision table
 * - ALL: AUCTION_ADMIN role
 */
@Service
public final class EditAuctionService implements EditAuctionUseCase {

    private static final Logger log = LoggerFactory.getLogger(EditAuctionService.class);

    private final AuctionRepository auctionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SpringDataUserSupervisionRepository supervisionRepository;

    public EditAuctionService(AuctionRepository auctionRepository, CurrentUserProvider currentUserProvider,
                               SpringDataUserSupervisionRepository supervisionRepository) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository);
        this.currentUserProvider = Objects.requireNonNull(currentUserProvider);
        this.supervisionRepository = supervisionRepository;
    }

    @Override
    public EditAuctionResult editAuction(EditAuctionCommand command) {
        var user = currentUserProvider.maybeCurrentUser().orElse(null);
        if (user == null) {
            log.warn("AUTHZ DENY userId=anonymous resourceId={} action=EDIT reason=NOT_AUTHENTICATED",
                    command.auctionId());
            return new EditAuctionResult.EditNotAllowed("Not authenticated");
        }

        var auctionOpt = auctionRepository.findById(command.auctionId());
        if (auctionOpt.isEmpty()) {
            log.info("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NOT_FOUND",
                    user.partyId().value(), command.auctionId());
            return new EditAuctionResult.EditNotAllowed("Auction not accessible");
        }

        Auction auction = auctionOpt.get();
        String userId = user.partyId().value();
        Set<String> roles = user.roles();

        // Check permissions based on Relation
        boolean canEdit = false;
        String matchedRelation = null;

        // OWN
        if (auction.createdByUserId().equals(userId)) {
            canEdit = true;
            matchedRelation = "OWN";
        }
        // ALL (admin)
        else if (roles.contains("AUCTION_ADMIN")) {
            canEdit = true;
            matchedRelation = "ALL";
        }
        // DIRECT_SUBORDINATE
        else if (supervisionRepository != null) {
            var subordinates = supervisionRepository.findByManagerUserId(userId);
            log.debug("DIRECT_SUBORDINATE check userId={} subordinates={} owner={}", userId, subordinates.size(), auction.createdByUserId());
            boolean isSubordinate = subordinates.stream()
                    .anyMatch(s -> s.getSubordinateUserId().equals(auction.createdByUserId()));
            if (isSubordinate) {
                canEdit = true;
                matchedRelation = "DIRECT_SUBORDINATE";
            }
        }

        if (!canEdit) {
            log.warn("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NO_PERMISSION roles={}",
                    userId, command.auctionId(), roles);
            return new EditAuctionResult.EditNotAllowed("Insufficient permissions");
        }

        if (auction.status() != AuctionStatus.DRAFT) {
            log.info("AUTHZ DENY userId={} resourceId={} action=EDIT reason=NOT_DRAFT status={}",
                    userId, command.auctionId(), auction.status());
            return new EditAuctionResult.EditNotAllowed("Only draft auctions can be edited");
        }

        // Perform edit
        Auction edited = auction.edit(command.title(), command.startingPrice());
        Auction saved = auctionRepository.save(edited);

        log.info("AUTHZ ALLOW userId={} resourceId={} action=EDIT relation={}",
                userId, command.auctionId(), matchedRelation);
        return new EditAuctionResult.AuctionEdited(AuctionViews.from(saved));
    }
}
