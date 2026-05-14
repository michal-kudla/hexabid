package com.github.hexabid.statement.port.in;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Read-only view of a participation decision, returned to the adapter layer.
 *
 * <p>Fields are populated according to the decision status:
 * <ul>
 *   <li>{@code PENDING} — {@code missingStatements} is populated</li>
 *   <li>{@code ADMITTED} — no additional fields</li>
 *   <li>{@code ADMITTED_WITH_CONDITIONS} — {@code conditions} is populated</li>
 *   <li>{@code REJECTED} — {@code rootCause}, {@code humanReason}, {@code cascadedStatements} are populated</li>
 * </ul>
 *
 * @param status              the decision status (PENDING, ADMITTED, ADMITTED_WITH_CONDITIONS, REJECTED)
 * @param rootCause           the statement code that caused rejection, or {@code null}
 * @param humanReason         a human-readable explanation for rejection, or {@code null}
 * @param missingStatements   statement codes that have not yet been answered
 * @param cascadedStatements  statement codes cancelled by a rejection cascade
 * @param conditions          conditions the candidate must satisfy for conditional admission
 */
public record ParticipationDecisionView(
        String status,
        @Nullable String rootCause,
        @Nullable String humanReason,
        List<String> missingStatements,
        List<String> cascadedStatements,
        List<String> conditions
) {}
