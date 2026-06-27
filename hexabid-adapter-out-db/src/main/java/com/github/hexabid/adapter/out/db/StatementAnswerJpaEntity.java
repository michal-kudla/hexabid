package com.github.hexabid.adapter.out.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA entity representing a single answer submitted within a statement program instance,
 * persisted in the {@code statement_answers} table.
 *
 * <p>Each answer is associated with a parent {@link StatementProgramInstanceJpaEntity} via a many-to-one
 * relationship and records the statement code, answer value, whether it is disqualifying,
 * and the submission timestamp.</p>
 */
@Entity
@Table(name = "statement_answers")
class StatementAnswerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private StatementProgramInstanceJpaEntity programInstance;

    @Column(nullable = false)
    private String statementCode;

    @Column(nullable = false)
    private String answerValue;

    @Column(nullable = false)
    private boolean disqualifying;

    @Column(columnDefinition = "UUID", nullable = false)
    private java.util.UUID answerId;

    @Column(nullable = false)
    private Instant submittedAt;

    StatementAnswerJpaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StatementProgramInstanceJpaEntity getProgramInstance() { return programInstance; }
    public void setProgramInstance(StatementProgramInstanceJpaEntity programInstance) { this.programInstance = programInstance; }
    public String getStatementCode() { return statementCode; }
    public void setStatementCode(String statementCode) { this.statementCode = statementCode; }
    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }
    public boolean isDisqualifying() { return disqualifying; }
    public void setDisqualifying(boolean disqualifying) { this.disqualifying = disqualifying; }
    public java.util.UUID getAnswerId() { return answerId; }
    public void setAnswerId(java.util.UUID answerId) { this.answerId = answerId; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
