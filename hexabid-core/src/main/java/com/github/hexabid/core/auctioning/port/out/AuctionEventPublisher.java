package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.event.AuctionDomainEvent;

public interface AuctionEventPublisher {
    void publish(AuctionDomainEvent event);
}
