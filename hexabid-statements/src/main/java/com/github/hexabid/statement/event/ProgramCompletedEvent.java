package com.github.hexabid.statement.event;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import java.time.Instant;
import java.util.Objects;

public record ProgramCompletedEvent(
        AuctionId auctionId,
        PartyId candidateId,
        Instant occurredAt
) implements StatementDomainEvent {
    public ProgramCompletedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() { return "PROGRAM_COMPLETED"; }
}
