package com.github.hexabid.statement.event;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.ParticipationPolicyTemplateId;
import java.time.Instant;
import java.util.Objects;

public record ProgramStartedEvent(
        AuctionId auctionId,
        PartyId candidateId,
        ParticipationPolicyTemplateId templateId,
        Instant occurredAt
) implements StatementDomainEvent {
    public ProgramStartedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(templateId, "templateId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() { return "PROGRAM_STARTED"; }
}
