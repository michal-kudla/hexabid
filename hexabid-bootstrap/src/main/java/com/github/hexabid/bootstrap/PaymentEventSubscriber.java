package com.github.hexabid.bootstrap;

import com.github.hexabid.core.auctioning.event.AuctionWonEvent;
import com.github.hexabid.payment.core.model.AccountId;
import com.github.hexabid.payment.core.usecase.ProcessPaymentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventSubscriber.class);

    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentEventSubscriber(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @EventListener
    public void onAuctionWon(AuctionWonEvent event) {
        log.info("Auction won: {} by {}", event.auctionId(), event.winnerId());
        processPaymentUseCase.execute(
                event.auctionId(),
                event.winningPrice(),
                "PLN",
                AccountId.next(),
                AccountId.next()
        );
    }
}
