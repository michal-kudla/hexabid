package com.acme.auctions.adapter.in.job;

import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsCommand;
import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsUseCase;
import com.acme.auctions.core.auctioning.port.in.ClosedAuctionsResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ExpiredAuctionScheduler {

    private final CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase;
    private final Clock clock;
    private final Counter closedAuctionsCounter;
    private final Counter closeConflictsCounter;

    public ExpiredAuctionScheduler(
            CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase,
            Clock clock,
            MeterRegistry meterRegistry
    ) {
        this.closeExpiredAuctionsUseCase = closeExpiredAuctionsUseCase;
        this.clock = clock;
        this.closedAuctionsCounter = meterRegistry.counter("auctions.close_expired.closed");
        this.closeConflictsCounter = meterRegistry.counter("auctions.close_expired.conflicts");
    }

    @Scheduled(fixedDelayString = "${auctions.jobs.close-expired.delay:60000}")
    public void checkExpiredAuctions() {
        ClosedAuctionsResult result = closeExpiredAuctionsUseCase.closeExpiredAuctions(
                new CloseExpiredAuctionsCommand(clock.instant())
        );
        closedAuctionsCounter.increment(result.closedCount());
        closeConflictsCounter.increment(result.conflictCount());
    }
}
