package com.github.hexabid.statement.event;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import java.time.Instant;

public sealed interface StatementDomainEvent permits
        ProgramStartedEvent,
        AnswerSubmittedEvent,
        ProgramCompletedEvent,
        ProgramRejectedEvent {

    AuctionId auctionId();
    PartyId candidateId();
    Instant occurredAt();
    String type();
}
