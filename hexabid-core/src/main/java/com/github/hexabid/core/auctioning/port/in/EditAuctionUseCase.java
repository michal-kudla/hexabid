package com.github.hexabid.core.auctioning.port.in;

/**
 * Use case edycji aukcji.
 */
public interface EditAuctionUseCase {
    EditAuctionResult editAuction(EditAuctionCommand command);
}
