package com.github.hexabid.statement.port.in;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Read-only view of a statement collection program, returned to the adapter layer.
 *
 * <p>This view contains the program's identity, template information, current status,
 * and three categorized lists of statement steps: available, completed, and blocked.
 *
 * @param programInstanceId  unique identifier of this program instance
 * @param auctionId           the auction this program qualifies the candidate for
 * @param candidateId         the candidate applying for participation
 * @param templateName        the name of the policy template (e.g. "PUBLIC_CONSUMER_LIGHT_V1")
 * @param templateVersion     the version of the policy template
 * @param status              the current lifecycle status (IN_PROGRESS, COMPLETED, REJECTED, CANCELLED)
 * @param availableStatements statements the candidate can currently answer
 * @param completedStatements statements the candidate has already answered
 * @param blockedStatements   statements whose prerequisites are not yet satisfied
 * @param decision            the current participation decision, or {@code null} if not yet evaluated
 */
public record StatementProgramView(
        UUID programInstanceId,
        UUID auctionId,
        String candidateId,
        String templateName,
        int templateVersion,
        String status,
        List<StatementStepView> availableStatements,
        List<StatementStepView> completedStatements,
        List<StatementStepView> blockedStatements,
        @Nullable ParticipationDecisionView decision
) {}
