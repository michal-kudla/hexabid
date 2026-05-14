package com.github.hexabid.statement.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Sealed hierarchy representing the outcome of a participation evaluation.
 *
 * <p>A candidate can be:
 * <ul>
 *   <li>{@link Admitted} — all blocking statements and conditions are satisfied</li>
 *   <li>{@link AdmittedWithConditions} — candidate may participate but must fulfil
 *       conditions before the final offer</li>
 *   <li>{@link Pending} — statements or evidence are still missing</li>
 *   <li>{@link Rejected} — a blocking violation or disqualifying answer exists</li>
 * </ul>
 *
 * <p>Important: {@code ADMITTED_WITH_CONDITIONS} must not be used to bypass
 * hard rejections. For sanctions, conflict of interest, refusal of terms,
 * and lack of legal capacity, the decision must always be {@link Rejected}.
 *
 * @see StatementProgramInstance
 */
public sealed interface ParticipationDecision {

    /**
     * The candidate this decision pertains to.
     */
    PartyId candidateId();

    /**
     * The auction this decision qualifies the candidate for.
     */
    AuctionId auctionId();

    /**
     * Candidate is fully admitted to participate in the auction.
     *
     * @param candidateId the admitted candidate
     * @param auctionId   the auction
     * @param decidedAt   when the decision was made
     */
    record Admitted(
            PartyId candidateId,
            AuctionId auctionId,
            Instant decidedAt
    ) implements ParticipationDecision {
        public Admitted {
            Objects.requireNonNull(candidateId);
            Objects.requireNonNull(auctionId);
            Objects.requireNonNull(decidedAt);
        }
    }

    /**
     * Candidate may participate but must satisfy conditions before the final offer.
     *
     * @param candidateId the conditionally admitted candidate
     * @param auctionId   the auction
     * @param conditions  conditions the candidate must satisfy
     * @param decidedAt   when the decision was made
     */
    record AdmittedWithConditions(
            PartyId candidateId,
            AuctionId auctionId,
            List<String> conditions,
            Instant decidedAt
    ) implements ParticipationDecision {
        public AdmittedWithConditions {
            Objects.requireNonNull(candidateId);
            Objects.requireNonNull(auctionId);
            conditions = List.copyOf(Objects.requireNonNull(conditions));
            Objects.requireNonNull(decidedAt);
        }
    }

    /**
     * Evaluation is incomplete — statements or evidence are still missing.
     *
     * <p>This is not a terminal decision. The program instance remains in
     * {@link ProgramInstanceStatus#IN_PROGRESS} and the candidate may continue
     * submitting answers.
     *
     * @param candidateId          the candidate
     * @param auctionId            the auction
     * @param missingStatements    statements that have not yet been answered
     * @param blockedByPrerequisites statements whose prerequisites are not met
     */
    record Pending(
            PartyId candidateId,
            AuctionId auctionId,
            List<StatementCode> missingStatements,
            List<StatementCode> blockedByPrerequisites
    ) implements ParticipationDecision {
        public Pending {
            Objects.requireNonNull(candidateId);
            Objects.requireNonNull(auctionId);
            missingStatements = List.copyOf(Objects.requireNonNull(missingStatements));
            blockedByPrerequisites = List.copyOf(Objects.requireNonNull(blockedByPrerequisites));
        }
    }

    /**
     * Candidate is rejected from participating in the auction.
     *
     * <p>Rejection is terminal within the current program instance. The root cause
     * identifies the statement whose disqualifying answer triggered the rejection.
     * The cascaded statements list contains all dependent statements that are
     * no longer required because the candidate is already rejected.
     *
     * @param candidateId        the rejected candidate
     * @param auctionId          the auction
     * @param rootCause         the statement code that triggered rejection
     * @param violationType     the classification of the violation
     * @param cascadedStatements statements cancelled by this rejection
     * @param humanReason       a human-readable explanation of why the candidate was rejected
     * @param decidedAt         when the decision was made
     */
    record Rejected(
            PartyId candidateId,
            AuctionId auctionId,
            StatementCode rootCause,
            StatementViolationType violationType,
            List<StatementCode> cascadedStatements,
            String humanReason,
            Instant decidedAt
    ) implements ParticipationDecision {
        public Rejected {
            Objects.requireNonNull(candidateId);
            Objects.requireNonNull(auctionId);
            Objects.requireNonNull(rootCause);
            Objects.requireNonNull(violationType);
            cascadedStatements = List.copyOf(Objects.requireNonNull(cascadedStatements));
            Objects.requireNonNull(humanReason);
            Objects.requireNonNull(decidedAt);
        }
    }
}
