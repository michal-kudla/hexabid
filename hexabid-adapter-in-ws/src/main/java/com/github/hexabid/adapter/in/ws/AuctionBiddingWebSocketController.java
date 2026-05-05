package com.github.hexabid.adapter.in.ws;

import com.github.hexabid.auth.core.identityaccess.model.AuthenticatedUser;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.PlaceBidCommand;
import com.github.hexabid.core.auctioning.port.in.PlaceBidFailureReason;
import com.github.hexabid.core.auctioning.port.in.PlaceBidResult;
import com.github.hexabid.core.auctioning.port.in.PlaceBidUseCase;
import com.github.hexabid.core.party.model.PartyId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
public class AuctionBiddingWebSocketController {

    private final PlaceBidUseCase placeBidUseCase;
    private final SimpMessagingTemplate messagingTemplate;
    private final Counter bidAcceptedCounter;
    private final Counter bidRejectedCounter;

    public AuctionBiddingWebSocketController(
            PlaceBidUseCase placeBidUseCase,
            SimpMessagingTemplate messagingTemplate,
            MeterRegistry meterRegistry
    ) {
        this.placeBidUseCase = placeBidUseCase;
        this.messagingTemplate = messagingTemplate;
        this.bidAcceptedCounter = meterRegistry.counter("auctions.bid.accepted");
        this.bidRejectedCounter = meterRegistry.counter("auctions.bid.rejected");
    }

    @MessageMapping("/auctions/{auctionId}/bids")
    public void placeBid(@DestinationVariable UUID auctionId, PlaceBidWebSocketMessage message,
                         Authentication authentication) {
        AuthenticatedUser authenticatedUser = resolveUser(authentication);
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

    private AuthenticatedUser resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            PartyId partyId = new PartyId("local:" + username);
            return new AuthenticatedUser(partyId, "local", username, username, username + "@example.com");
        }
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String partyId = attribute(oAuth2User, "partyId");
            String provider = attribute(oAuth2User, "provider");
            String subject = attribute(oAuth2User, "subject");
            String displayName = attribute(oAuth2User, "displayName");
            String email = attribute(oAuth2User, "email");
            if (partyId == null || provider == null || subject == null || displayName == null) {
                return null;
            }
            return new AuthenticatedUser(new PartyId(partyId), provider, subject, displayName, email);
        }
        return null;
    }

    private static String attribute(OAuth2User user, String name) {
        Object value = user.getAttributes().get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        return text;
    }
}
