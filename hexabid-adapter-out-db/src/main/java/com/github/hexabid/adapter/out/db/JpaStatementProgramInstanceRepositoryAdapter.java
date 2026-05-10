package com.github.hexabid.adapter.out.db;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.ParticipationDecision;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.PolicyTemplateVersion;
import com.github.hexabid.statement.model.ProgramInstanceStatus;
import com.github.hexabid.statement.model.StatementAnswer;
import com.github.hexabid.statement.model.StatementAnswerId;
import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementProgramInstance;
import com.github.hexabid.statement.model.StatementProgramInstanceId;
import com.github.hexabid.statement.model.ParticipationPolicyTemplateId;
import com.github.hexabid.statement.model.StatementViolationType;
import com.github.hexabid.statement.port.out.StatementProgramInstanceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA outbound adapter that implements the {@link StatementProgramInstanceRepository} port.
 *
 * <p>Persists and retrieves {@link StatementProgramInstance} domain objects via Spring Data JPA,
 * handling bidirectional mapping between domain models and JPA entities, including
 * embedded decision state and answer collections.</p>
 */
@Repository
class JpaStatementProgramInstanceRepositoryAdapter implements StatementProgramInstanceRepository {

    private final SpringDataStatementProgramInstanceJpaRepository jpaRepository;

    JpaStatementProgramInstanceRepositoryAdapter(SpringDataStatementProgramInstanceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Persists a statement program instance, mapping the domain object to a JPA entity
     * and flushing it to the database.
     *
     * @param instance the domain model to persist
     * @return the rehydrated domain model reflecting the persisted state
     */
    @Override
    public StatementProgramInstance save(StatementProgramInstance instance) {
        StatementProgramInstanceJpaEntity entity = toEntity(instance);
        StatementProgramInstanceJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    /**
     * Finds a statement program instance by its auction and candidate identifiers.
     *
     * @param auctionId  the auction identifier
     * @param candidateId the candidate (party) identifier
     * @return an {@link Optional} containing the matching instance, or empty if not found
     */
    @Override
    public Optional<StatementProgramInstance> findByAuctionIdAndCandidateId(AuctionId auctionId, PartyId candidateId) {
        return jpaRepository.findByAuctionIdAndCandidateId(auctionId.value(), candidateId.value())
                .map(this::toDomain);
    }

    /**
     * Finds a statement program instance by its unique identifier.
     *
     * @param id the program instance identifier
     * @return an {@link Optional} containing the matching instance, or empty if not found
     */
    @Override
    public Optional<StatementProgramInstance> findById(StatementProgramInstanceId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    private StatementProgramInstanceJpaEntity toEntity(StatementProgramInstance instance) {
        StatementProgramInstanceJpaEntity entity = new StatementProgramInstanceJpaEntity();
        entity.setId(instance.id().value());
        entity.setAuctionId(instance.auctionId().value());
        entity.setCandidateId(instance.candidateId().value());
        entity.setTemplateId(instance.templateId().value());
        entity.setTemplateVersion(instance.templateVersion().value());
        entity.setStatus(instance.status().name());
        entity.setCreatedAt(instance.createdAt());

        if (instance.decision() != null) {
            entity.setDecisionStatus(toDecisionStatus(instance.decision()));
            if (instance.decision() instanceof ParticipationDecision.Rejected rejected) {
                entity.setDecisionViolationType(rejected.violationType().name());
                entity.setDecisionRootCause(rejected.rootCause().value());
                entity.setDecisionHumanReason(rejected.humanReason());
                entity.setDecisionDecidedAt(rejected.decidedAt());
                if (!rejected.cascadedStatements().isEmpty()) {
                    entity.setDecisionCascadedStatements(String.join(",", rejected.cascadedStatements().stream().map(StatementCode::value).toList()));
                }
            } else if (instance.decision() instanceof ParticipationDecision.Pending pending) {
                if (!pending.missingStatements().isEmpty()) {
                    entity.setDecisionMissingStatements(String.join(",", pending.missingStatements().stream().map(StatementCode::value).toList()));
                }
                if (!pending.blockedByPrerequisites().isEmpty()) {
                    entity.setDecisionBlockedByPrerequisites(String.join(",", pending.blockedByPrerequisites().stream().map(StatementCode::value).toList()));
                }
            } else if (instance.decision() instanceof ParticipationDecision.Admitted admitted) {
                entity.setDecisionDecidedAt(admitted.decidedAt());
            } else if (instance.decision() instanceof ParticipationDecision.AdmittedWithConditions awc) {
                entity.setDecisionDecidedAt(awc.decidedAt());
                if (!awc.conditions().isEmpty()) {
                    entity.setDecisionConditions(String.join(",", awc.conditions()));
                }
            }
        }

        entity.getAnswers().clear();
        for (StatementAnswer answer : instance.answers().values()) {
            StatementAnswerJpaEntity answerEntity = new StatementAnswerJpaEntity();
            answerEntity.setProgramInstance(entity);
            answerEntity.setStatementCode(answer.statementCode().value());
            answerEntity.setAnswerValue(answer.answerValue());
            answerEntity.setDisqualifying(answer.disqualifying());
            answerEntity.setAnswerId(answer.id().value());
            answerEntity.setSubmittedAt(answer.submittedAt());
            entity.getAnswers().add(answerEntity);
        }

        return entity;
    }

    private StatementProgramInstance toDomain(StatementProgramInstanceJpaEntity entity) {
        var answers = new java.util.LinkedHashMap<StatementCode, StatementAnswer>();
        for (StatementAnswerJpaEntity answerEntity : entity.getAnswers()) {
            StatementCode code = new StatementCode(answerEntity.getStatementCode());
            StatementAnswer answer = new StatementAnswer(
                    new StatementAnswerId(answerEntity.getAnswerId()),
                    new StatementProgramInstanceId(entity.getId()),
                    code,
                    answerEntity.getAnswerValue(),
                    answerEntity.isDisqualifying(),
                    answerEntity.getSubmittedAt()
            );
            answers.put(code, answer);
        }

        ParticipationDecision decision = null;
        if (entity.getDecisionStatus() != null) {
            PartyId candidateId = PartyId.of(entity.getCandidateId());
            AuctionId auctionId = AuctionId.of(entity.getAuctionId());
            decision = switch (entity.getDecisionStatus()) {
                case "ADMITTED" -> new ParticipationDecision.Admitted(candidateId, auctionId,
                        entity.getDecisionDecidedAt() != null ? entity.getDecisionDecidedAt() : entity.getCreatedAt());
                case "ADMITTED_WITH_CONDITIONS" -> new ParticipationDecision.AdmittedWithConditions(
                        candidateId, auctionId,
                        entity.getDecisionConditions() != null ? List.of(entity.getDecisionConditions().split(",")) : List.of(),
                        entity.getDecisionDecidedAt() != null ? entity.getDecisionDecidedAt() : entity.getCreatedAt());
                case "REJECTED" -> new ParticipationDecision.Rejected(
                        candidateId, auctionId,
                        new StatementCode(entity.getDecisionRootCause()),
                        entity.getDecisionViolationType() != null
                                ? StatementViolationType.valueOf(entity.getDecisionViolationType())
                                : StatementViolationType.FATAL_DECLARATION,
                        entity.getDecisionCascadedStatements() != null
                                ? List.of(entity.getDecisionCascadedStatements().split(",")).stream().map(StatementCode::new).toList()
                                : List.of(),
                        entity.getDecisionHumanReason(),
                        entity.getDecisionDecidedAt() != null ? entity.getDecisionDecidedAt() : entity.getCreatedAt());
                case "PENDING" -> new ParticipationDecision.Pending(
                        candidateId, auctionId,
                        entity.getDecisionMissingStatements() != null
                                ? List.of(entity.getDecisionMissingStatements().split(",")).stream().map(StatementCode::new).toList()
                                : List.of(),
                        entity.getDecisionBlockedByPrerequisites() != null
                                ? List.of(entity.getDecisionBlockedByPrerequisites().split(",")).stream().map(StatementCode::new).toList()
                                : List.of());
                default -> throw new IllegalStateException("Unknown decision status: " + entity.getDecisionStatus());
            };
        }

        return StatementProgramInstance.rehydrate(
                new StatementProgramInstanceId(entity.getId()),
                AuctionId.of(entity.getAuctionId()),
                PartyId.of(entity.getCandidateId()),
                new ParticipationPolicyTemplateId(entity.getTemplateId()),
                PolicyTemplateVersion.of(entity.getTemplateVersion()),
                answers,
                ProgramInstanceStatus.valueOf(entity.getStatus()),
                decision,
                entity.getCreatedAt()
        );
    }

    private String toDecisionStatus(ParticipationDecision decision) {
        return switch (decision) {
            case ParticipationDecision.Admitted ignored -> "ADMITTED";
            case ParticipationDecision.AdmittedWithConditions ignored -> "ADMITTED_WITH_CONDITIONS";
            case ParticipationDecision.Pending ignored -> "PENDING";
            case ParticipationDecision.Rejected ignored -> "REJECTED";
        };
    }
}
