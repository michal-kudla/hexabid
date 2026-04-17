package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.auth.core.identityaccess.port.in.CurrentUserProfileView;
import com.github.hexabid.contract.model.AuctionListItemResponse;
import com.github.hexabid.contract.model.AuctionListResponse;
import com.github.hexabid.contract.model.AuctionResponse;
import com.github.hexabid.contract.model.AuctionStatus;
import com.github.hexabid.contract.model.BidResponse;
import com.github.hexabid.contract.model.CurrentUserProfileResponse;
import com.github.hexabid.core.auctioning.port.in.AuctionView;
import com.github.hexabid.core.auctioning.port.in.AuctionBrowsePage;
import com.github.hexabid.core.auctioning.port.in.AuctionSummaryView;
import com.github.hexabid.core.auctioning.port.in.BidView;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
class RestAuctionContractMapper {

    AuctionListResponse toResponse(AuctionBrowsePage page) {
        return new AuctionListResponse(page.items().stream().map(this::toResponse).toList())
                .nextCursor(page.nextCursor());
    }

    AuctionResponse toResponse(AuctionView view) {
        return new AuctionResponse(
                view.auctionId(),
                view.sellerId(),
                view.title(),
                view.currentPrice(),
                view.currency(),
                OffsetDateTime.ofInstant(view.endsAt(), ZoneOffset.UTC),
                AuctionStatus.fromValue(view.status().name()),
                view.biddingHistory().stream().map(this::toResponse).toList()
        ).leadingBidderId(view.leadingBidderId());
    }

    AuctionListItemResponse toResponse(AuctionSummaryView view) {
        return new AuctionListItemResponse(
                view.auctionId(),
                view.sellerId(),
                view.title(),
                view.currentPrice(),
                view.currency(),
                OffsetDateTime.ofInstant(view.endsAt(), ZoneOffset.UTC),
                AuctionStatus.fromValue(view.status().name())
        ).leadingBidderId(view.leadingBidderId());
    }

    BidResponse toResponse(BidView view) {
        return new BidResponse(
                view.bidderId(),
                view.amount(),
                view.currency(),
                OffsetDateTime.ofInstant(view.placedAt(), ZoneOffset.UTC)
        );
    }

    CurrentUserProfileResponse toResponse(CurrentUserProfileView view) {
        return new CurrentUserProfileResponse(
                view.partyId(),
                view.provider(),
                view.displayName(),
                view.verified()
        ).email(view.email());
    }
}
