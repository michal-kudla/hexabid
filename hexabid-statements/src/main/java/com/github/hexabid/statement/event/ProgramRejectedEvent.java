package com.github.hexabid.statement.event;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.StatementCode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record ProgramRejectedEvent(
        AuctionId auctionId,
        PartyId candidateId,
        StatementCode rootCause,
        String humanReason,
        List<StatementCode> cascadedStatements,
        Instant occurredAt
) implements StatementDomainEvent {
    public ProgramRejectedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(rootCause, "rootCause must not be null");
        Objects.requireNonNull(humanReason, "humanReason must not be null");
        Objects.requireNonNull(cascadedStatements, "cascadedStatements must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() { return "PROGRAM_REJECTED"; }
}
