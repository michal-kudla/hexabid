package com.github.hexabid.statement.event;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.StatementCode;
import java.time.Instant;
import java.util.Objects;

public record AnswerSubmittedEvent(
        AuctionId auctionId,
        PartyId candidateId,
        StatementCode statementCode,
        String answerValue,
        boolean disqualifying,
        Instant occurredAt
) implements StatementDomainEvent {
    public AnswerSubmittedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(statementCode, "statementCode must not be null");
        Objects.requireNonNull(answerValue, "answerValue must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() { return "ANSWER_SUBMITTED"; }
}
