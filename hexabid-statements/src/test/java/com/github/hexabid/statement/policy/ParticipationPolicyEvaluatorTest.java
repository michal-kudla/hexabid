package com.github.hexabid.statement.policy;

import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementAnswer;
import com.github.hexabid.statement.model.StatementAnswerId;
import com.github.hexabid.statement.model.StatementProgramInstanceId;
import com.github.hexabid.statement.model.StatementViolationType;
import com.github.hexabid.statement.template.PolicyTemplateCatalog;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParticipationPolicyEvaluatorTest {

    private final ParticipationPolicyEvaluator evaluator = new ParticipationPolicyEvaluator(Clock.systemUTC());

    @Test
    void evaluateReturnsAdmittedWhenAllStatementsAnsweredPositively() {
        var template = PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1;

        Map<StatementCode, StatementAnswer> answers = Map.of(
                StatementCode.LEGAL_CAPACITY, answerFor(StatementCode.LEGAL_CAPACITY, "YES"),
                StatementCode.SANCTIONS_CLEARANCE, answerFor(StatementCode.SANCTIONS_CLEARANCE, "YES"),
                StatementCode.PAYMENT_READINESS, answerFor(StatementCode.PAYMENT_READINESS, "YES"),
                StatementCode.TERMS_ACCEPTANCE, answerFor(StatementCode.TERMS_ACCEPTANCE, "YES")
        );

        var result = evaluator.evaluate(template, answers);

        assertEquals(ParticipationPolicyEvaluator.EvaluationStatus.ADMITTED, result.status());
    }

    @Test
    void evaluateReturnsRejectedWhenDisqualifyingAnswerSubmitted() {
        var template = PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1;

        Map<StatementCode, StatementAnswer> answers = Map.of(
                StatementCode.LEGAL_CAPACITY, answerFor(StatementCode.LEGAL_CAPACITY, "YES"),
                StatementCode.SANCTIONS_CLEARANCE, disqualifyingAnswerFor(StatementCode.SANCTIONS_CLEARANCE, "NO")
        );

        var result = evaluator.evaluate(template, answers);

        assertEquals(ParticipationPolicyEvaluator.EvaluationStatus.REJECTED, result.status());
        assertEquals(StatementCode.SANCTIONS_CLEARANCE, result.rootCause());
        assertTrue(result.cascadedStatements().contains(StatementCode.PAYMENT_READINESS));
        assertTrue(result.cascadedStatements().contains(StatementCode.TERMS_ACCEPTANCE));
    }

    @Test
    void evaluateReturnsPendingWhenStatementsAreMissing() {
        var template = PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1;

        Map<StatementCode, StatementAnswer> answers = Map.of(
                StatementCode.LEGAL_CAPACITY, answerFor(StatementCode.LEGAL_CAPACITY, "YES"),
                StatementCode.SANCTIONS_CLEARANCE, answerFor(StatementCode.SANCTIONS_CLEARANCE, "YES")
        );

        var result = evaluator.evaluate(template, answers);

        assertEquals(ParticipationPolicyEvaluator.EvaluationStatus.PENDING, result.status());
        assertTrue(result.missingStatements().contains(StatementCode.PAYMENT_READINESS));
        assertTrue(result.missingStatements().contains(StatementCode.TERMS_ACCEPTANCE));
    }

    @Test
    void evaluateDetectsFatalViolation() {
        var template = PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1;

        Map<StatementCode, StatementAnswer> answers = Map.of(
                StatementCode.LEGAL_CAPACITY, answerFor(StatementCode.LEGAL_CAPACITY, "YES"),
                StatementCode.SANCTIONS_CLEARANCE, disqualifyingAnswerFor(StatementCode.SANCTIONS_CLEARANCE, "NO")
        );

        var result = evaluator.evaluate(template, answers);

        assertFalse(result.violations().isEmpty());
        assertTrue(result.violations().get(0).fatal());
        assertEquals(StatementViolationType.FATAL_DECLARATION, result.violations().get(0).type());
    }

    private StatementAnswer answerFor(StatementCode code, String value) {
        return new StatementAnswer(
                StatementAnswerId.newId(), StatementProgramInstanceId.newId(),
                code, value, false, Instant.now()
        );
    }

    private StatementAnswer disqualifyingAnswerFor(StatementCode code, String value) {
        return new StatementAnswer(
                StatementAnswerId.newId(), StatementProgramInstanceId.newId(),
                code, value, true, Instant.now()
        );
    }
}
