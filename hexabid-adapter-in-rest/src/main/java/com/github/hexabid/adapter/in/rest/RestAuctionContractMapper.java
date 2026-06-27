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
        var response = new AuctionResponse(
                view.auctionId(),
                view.sellerId(),
                view.title(),
                view.currentPrice(),
                view.currency(),
                OffsetDateTime.ofInstant(view.endsAt(), ZoneOffset.UTC),
                AuctionStatus.fromValue(view.status().name()),
                view.biddingHistory().stream().map(this::toResponse).toList()
        ).leadingBidderId(view.leadingBidderId());

        if (view.participationPolicyTemplate() != null) {
            response.setQualificationSummary(new com.github.hexabid.contract.model.AuctionQualificationSummary()
                    .participationPolicyTemplate(view.participationPolicyTemplate())
                    .templateLabel(getTemplateLabel(view.participationPolicyTemplate()))
                    .taskCount(getTemplateTaskCount(view.participationPolicyTemplate())));
        }

        return response;
    }

    private static int getTemplateTaskCount(String templateName) {
        return switch (templateName) {
            case "PUBLIC_CONSUMER_LIGHT_V1" -> 4;
            case "REGULATED_ASSET_BUYER_V1" -> 8;
            case "HIGH_VALUE_TENDER_V1" -> 11;
            default -> 0;
        };
    }

    private static String getTemplateLabel(String templateName) {
        return switch (templateName) {
            case "PUBLIC_CONSUMER_LIGHT_V1" -> "Standardowy konsument";
            case "REGULATED_ASSET_BUYER_V1" -> "Nabywca regulowany";
            case "HIGH_VALUE_TENDER_V1" -> "Przetarg wysokiej wartości";
            default -> templateName;
        };
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
