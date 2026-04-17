package com.github.hexabid.adapter.in.ws;

import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.PlaceBidCommand;
import com.github.hexabid.core.auctioning.port.in.PlaceBidFailureReason;
import com.github.hexabid.core.auctioning.port.in.PlaceBidResult;
import com.github.hexabid.core.auctioning.port.in.PlaceBidUseCase;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
public class AuctionBiddingWebSocketController {

    private final PlaceBidUseCase placeBidUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final SimpMessagingTemplate messagingTemplate;
    private final Counter bidAcceptedCounter;
    private final Counter bidRejectedCounter;

    public AuctionBiddingWebSocketController(
            PlaceBidUseCase placeBidUseCase,
            CurrentUserProvider currentUserProvider,
            SimpMessagingTemplate messagingTemplate,
            MeterRegistry meterRegistry
    ) {
        this.placeBidUseCase = placeBidUseCase;
        this.currentUserProvider = currentUserProvider;
        this.messagingTemplate = messagingTemplate;
        this.bidAcceptedCounter = meterRegistry.counter("auctions.bid.accepted");
        this.bidRejectedCounter = meterRegistry.counter("auctions.bid.rejected");
    }

    @MessageMapping("/auctions/{auctionId}/bids")
    public void placeBid(@DestinationVariable UUID auctionId, PlaceBidWebSocketMessage message) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            bidRejectedCounter.increment();
            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/errors",
                    new BidRejectedWebSocketMessage("UNAUTHENTICATED", "authentication is required")
            );
            return;
        }
        PlaceBidResult result = placeBidUseCase.placeBid(new PlaceBidCommand(
                new AuctionId(auctionId),
                authenticatedUser.partyId(),
                new Price(new BigDecimal(message.amount()), message.currency())
        ));
        if (!(result instanceof PlaceBidResult.BidRejected(
                PlaceBidFailureReason reason, String message1
        ))) {
            PlaceBidResult.BidAccepted accepted = (PlaceBidResult.BidAccepted) result;
            var placedBid = accepted.bid();

            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/bids",
                    new BidAcceptedWebSocketMessage(
                            placedBid.bidderId(),
                            placedBid.amount(),
                            placedBid.currency(),
                            placedBid.placedAt().toString()
                    )
            );
            bidAcceptedCounter.increment();
        } else {
            bidRejectedCounter.increment();
            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/errors",
                    new BidRejectedWebSocketMessage(reason.name(), message1)
            );
            return;
        }
    }
}
