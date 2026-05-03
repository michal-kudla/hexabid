package com.github.hexabid.core.auctioning.port.in;

public interface SettleAuctionUseCase {
    SettlementResult settleAuction(SettleAuctionCommand command);
}
