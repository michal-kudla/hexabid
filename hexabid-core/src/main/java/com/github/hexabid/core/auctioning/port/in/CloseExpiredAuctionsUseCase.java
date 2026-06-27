package com.github.hexabid.core.auctioning.port.in;

public interface CloseExpiredAuctionsUseCase {
    ClosedAuctionsResult closeExpiredAuctions(CloseExpiredAuctionsCommand command);
}
