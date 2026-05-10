package com.github.hexabid.statement.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementDefinitionTest {

    @Test
    void isDisqualifyingAnswerReturnsTrueForMatchingAnswer() {
        StatementDefinition def = new StatementDefinition(
                StatementCode.SANCTIONS_CLEARANCE, StatementCategory.COMPLIANCE,
                "Brak sankcji", "Czy jesteś na liście?", AnswerType.YES_NO,
                StatementSeverity.BLOCKING,
                List.of(DisqualifyingAnswer.of("NO", "Osoba sankcyjna")),
                List.of()
        );

        assertTrue(def.isDisqualifyingAnswer("NO"));
        assertFalse(def.isDisqualifyingAnswer("YES"));
    }

    @Test
    void isDisqualifyingAnswerReturnsFalseWhenNoDisqualifyingAnswers() {
        StatementDefinition def = new StatementDefinition(
                StatementCode.PEP_DISCLOSURE, StatementCategory.COMPLIANCE,
                "PEP", "Czy jesteś PEP?", AnswerType.YES_NO,
                StatementSeverity.IMPORTANT,
                List.of(),
                List.of()
        );

        assertFalse(def.isDisqualifyingAnswer("YES"));
        assertFalse(def.isDisqualifyingAnswer("NO"));
    }
}
