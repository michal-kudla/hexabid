package com.acme.auctions.payment.core.infrastructure;

import com.acme.auctions.core.auctioning.event.AuctionWonEvent;
import com.acme.auctions.payment.core.usecase.ProcessPaymentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for AuctionWonEvent that triggers the payment process.
 */
@Component
public class AuctionWonEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuctionWonEventListener.class);

    private final ProcessPaymentUseCase processPaymentUseCase;

    public AuctionWonEventListener(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @EventListener
    public void onAuctionWon(AuctionWonEvent event) {
        log.info("Auction won: {} by {}", event.auctionId(), event.winnerId());
        
        // In a real application, we would determine the currency based on the bidder's preference 
        // or the auction's settings. For this demo, we use PLN or BTC for crypto.
        String targetCurrency = "PLN"; 
        processPaymentUseCase.execute(event.auctionId(), event.winningPrice(), targetCurrency);
    }
}
