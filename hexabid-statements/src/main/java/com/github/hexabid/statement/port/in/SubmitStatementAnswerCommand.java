package com.github.hexabid.statement.port.in;

import java.util.UUID;

/**
 * Command to submit an answer to a statement within a program.
 *
 * @param auctionId     the auction the candidate is applying to participate in
 * @param candidateId   the identifier of the candidate submitting the answer
 * @param statementCode the unique code identifying the statement being answered
 * @param answerValue   the candidate's answer value
 */
public record SubmitStatementAnswerCommand(
        UUID auctionId,
        String candidateId,
        String statementCode,
        String answerValue
) {}
