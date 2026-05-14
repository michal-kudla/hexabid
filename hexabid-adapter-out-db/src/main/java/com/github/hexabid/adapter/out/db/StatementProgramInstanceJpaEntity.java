package com.github.hexabid.adapter.out.db;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing a statement program instance persisted in the {@code statement_program_instances} table.
 *
 * <p>Stores the core participation state including auction and candidate association, template reference,
 * current status, the participation decision (if any), and a one-to-many collection of
 * {@link StatementAnswerJpaEntity} answers.</p>
 */
@Entity
@Table(name = "statement_program_instances")
class StatementProgramInstanceJpaEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID auctionId;

    @Column(nullable = false)
    private String candidateId;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID templateId;

    @Column(nullable = false)
    private int templateVersion;

    @Column(nullable = false)
    private String status;

    private String decisionStatus;
    private String decisionViolationType;
    private String decisionRootCause;
    private String decisionHumanReason;

    @Column(columnDefinition = "TEXT")
    private String decisionMissingStatements;

    @Column(columnDefinition = "TEXT")
    private String decisionCascadedStatements;

    @Column(columnDefinition = "TEXT")
    private String decisionBlockedByPrerequisites;

    @Column(columnDefinition = "TEXT")
    private String decisionConditions;

    private Instant decisionDecidedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "programInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatementAnswerJpaEntity> answers = new ArrayList<>();

    StatementProgramInstanceJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAuctionId() { return auctionId; }
    public void setAuctionId(UUID auctionId) { this.auctionId = auctionId; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public int getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(int templateVersion) { this.templateVersion = templateVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecisionStatus() { return decisionStatus; }
    public void setDecisionStatus(String decisionStatus) { this.decisionStatus = decisionStatus; }
    public String getDecisionViolationType() { return decisionViolationType; }
    public void setDecisionViolationType(String decisionViolationType) { this.decisionViolationType = decisionViolationType; }
    public String getDecisionRootCause() { return decisionRootCause; }
    public void setDecisionRootCause(String decisionRootCause) { this.decisionRootCause = decisionRootCause; }
    public String getDecisionHumanReason() { return decisionHumanReason; }
    public void setDecisionHumanReason(String decisionHumanReason) { this.decisionHumanReason = decisionHumanReason; }
    public String getDecisionMissingStatements() { return decisionMissingStatements; }
    public void setDecisionMissingStatements(String decisionMissingStatements) { this.decisionMissingStatements = decisionMissingStatements; }
    public String getDecisionCascadedStatements() { return decisionCascadedStatements; }
    public void setDecisionCascadedStatements(String decisionCascadedStatements) { this.decisionCascadedStatements = decisionCascadedStatements; }
    public String getDecisionBlockedByPrerequisites() { return decisionBlockedByPrerequisites; }
    public void setDecisionBlockedByPrerequisites(String decisionBlockedByPrerequisites) { this.decisionBlockedByPrerequisites = decisionBlockedByPrerequisites; }
    public String getDecisionConditions() { return decisionConditions; }
    public void setDecisionConditions(String decisionConditions) { this.decisionConditions = decisionConditions; }
    public Instant getDecisionDecidedAt() { return decisionDecidedAt; }
    public void setDecisionDecidedAt(Instant decisionDecidedAt) { this.decisionDecidedAt = decisionDecidedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<StatementAnswerJpaEntity> getAnswers() { return answers; }
    public void setAnswers(List<StatementAnswerJpaEntity> answers) { this.answers = answers; }
}
