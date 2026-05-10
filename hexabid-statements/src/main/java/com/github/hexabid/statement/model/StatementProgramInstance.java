package com.github.hexabid.statement.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root representing an instance of a statement collection program
 * for a specific candidate participating in a specific auction.
 *
 * <p>A program instance tracks the lifecycle of a candidate's participation
 * qualification: from the initial {@link ProgramInstanceStatus#IN_PROGRESS} state,
 * through answer submission, to a terminal {@link ProgramInstanceStatus#COMPLETED}
 * or {@link ProgramInstanceStatus#REJECTED} state.
 *
 * <p>Instances are created via {@link #create} and reconstructed from persistence
 * via {@link #rehydrate}. State transitions occur through {@link #submitAnswer},
 * {@link #markRejected}, and {@link #markCompleted}.
 *
 * @see ParticipationDecision
 * @see StatementAnswer
 */
public final class StatementProgramInstance {

    private final StatementProgramInstanceId id;
    private final AuctionId auctionId;
    private final PartyId candidateId;
    private final ParticipationPolicyTemplateId templateId;
    private final PolicyTemplateVersion templateVersion;
    private final Map<StatementCode, StatementAnswer> answers;
    private ProgramInstanceStatus status;
    private ParticipationDecision decision;
    private final Instant createdAt;

    private StatementProgramInstance(
            StatementProgramInstanceId id,
            AuctionId auctionId,
            PartyId candidateId,
            ParticipationPolicyTemplateId templateId,
            PolicyTemplateVersion templateVersion,
            Map<StatementCode, StatementAnswer> answers,
            ProgramInstanceStatus status,
            ParticipationDecision decision,
            Instant createdAt
    ) {
        this.id = id;
        this.auctionId = auctionId;
        this.candidateId = candidateId;
        this.templateId = templateId;
        this.templateVersion = templateVersion;
        this.answers = answers;
        this.status = status;
        this.decision = decision;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new program instance in {@link ProgramInstanceStatus#IN_PROGRESS} status
     * with no answers and no decision.
     *
     * @param auctionId       the auction this program qualifies the candidate for
     * @param candidateId     the candidate applying for participation
     * @param templateId      the policy template this program follows
     * @param templateVersion the version of the policy template
     * @param createdAt       the instant of creation
     * @return a new program instance
     */
    public static StatementProgramInstance create(
            AuctionId auctionId,
            PartyId candidateId,
            ParticipationPolicyTemplateId templateId,
            PolicyTemplateVersion templateVersion,
            Instant createdAt
    ) {
        return new StatementProgramInstance(
                StatementProgramInstanceId.newId(),
                auctionId,
                candidateId,
                templateId,
                templateVersion,
                new LinkedHashMap<>(),
                ProgramInstanceStatus.IN_PROGRESS,
                null,
                createdAt
        );
    }

    /**
     * Reconstructs a program instance from persistence.
     *
     * <p>Unlike {@link #create}, this method accepts all fields directly
     * and does not generate new identifiers or default any state.
     *
     * @param id              the persistent identifier
     * @param auctionId       the auction identifier
     * @param candidateId     the candidate identifier
     * @param templateId      the policy template identifier
     * @param templateVersion the policy template version
     * @param answers         the map of submitted answers keyed by statement code
     * @param status          the current lifecycle status
     * @param decision        the current participation decision, may be {@code null}
     * @param createdAt       the instant of creation
     * @return a reconstituted program instance
     */
    public static StatementProgramInstance rehydrate(
            StatementProgramInstanceId id,
            AuctionId auctionId,
            PartyId candidateId,
            ParticipationPolicyTemplateId templateId,
            PolicyTemplateVersion templateVersion,
            Map<StatementCode, StatementAnswer> answers,
            ProgramInstanceStatus status,
            ParticipationDecision decision,
            Instant createdAt
    ) {
        return new StatementProgramInstance(id, auctionId, candidateId, templateId, templateVersion,
                new LinkedHashMap<>(answers), status, decision, createdAt);
    }

    /**
     * Submits an answer to a statement within this program instance.
     *
     * <p>The instance must be in {@link ProgramInstanceStatus#IN_PROGRESS} status.
     * If the instance has already been rejected or completed, an
     * {@link IllegalStateException} is thrown.
     *
     * @param answer the answer to submit
     * @throws IllegalStateException if the instance is not in progress
     * @throws NullPointerException  if the answer is null
     */
    public void submitAnswer(StatementAnswer answer) {
        Objects.requireNonNull(answer, "answer must not be null");
        if (status != ProgramInstanceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit answer to program instance with status: " + status);
        }
        answers.put(answer.statementCode(), answer);
    }

    /**
     * Transitions this instance to {@link ProgramInstanceStatus#REJECTED}
     * with the given rejection decision.
     *
     * @param rejected the rejection decision
     * @throws NullPointerException if the decision is null
     */
    public void markRejected(ParticipationDecision.Rejected rejected) {
        Objects.requireNonNull(rejected, "rejected decision must not be null");
        this.status = ProgramInstanceStatus.REJECTED;
        this.decision = rejected;
    }

    /**
     * Transitions this instance to a terminal state based on the type of decision.
     *
     * <p>{@link ParticipationDecision.Rejected} transitions to
     * {@link ProgramInstanceStatus#REJECTED}.
     * {@link ParticipationDecision.Admitted} and
     * {@link ParticipationDecision.AdmittedWithConditions} transition to
     * {@link ProgramInstanceStatus#COMPLETED}.
     * {@link ParticipationDecision.Pending} keeps the instance in
     * {@link ProgramInstanceStatus#IN_PROGRESS} but records the decision
     * for informational purposes.
     *
     * @param decision the participation decision
     */
    public void markCompleted(ParticipationDecision decision) {
        this.decision = decision;
        switch (decision) {
            case ParticipationDecision.Rejected ignored -> this.status = ProgramInstanceStatus.REJECTED;
            case ParticipationDecision.Admitted ignored -> this.status = ProgramInstanceStatus.COMPLETED;
            case ParticipationDecision.AdmittedWithConditions ignored -> this.status = ProgramInstanceStatus.COMPLETED;
            case ParticipationDecision.Pending ignored -> { /* stays IN_PROGRESS */ }
        }
    }

    /**
     * Checks whether an answer has been submitted for the given statement.
     *
     * @param code the statement code to check
     * @return {@code true} if an answer exists for the statement
     */
    public boolean hasAnswerFor(StatementCode code) {
        return answers.containsKey(code);
    }

    /**
     * Returns the answer submitted for the given statement, or {@code null}
     * if no answer has been submitted.
     *
     * @param code the statement code to look up
     * @return the answer, or {@code null}
     */
    public StatementAnswer getAnswer(StatementCode code) {
        return answers.get(code);
    }

    public StatementProgramInstanceId id() { return id; }
    public AuctionId auctionId() { return auctionId; }
    public PartyId candidateId() { return candidateId; }
    public ParticipationPolicyTemplateId templateId() { return templateId; }
    public PolicyTemplateVersion templateVersion() { return templateVersion; }
    public Map<StatementCode, StatementAnswer> answers() { return Collections.unmodifiableMap(answers); }
    public ProgramInstanceStatus status() { return status; }
    public ParticipationDecision decision() { return decision; }
    public Instant createdAt() { return createdAt; }
}
