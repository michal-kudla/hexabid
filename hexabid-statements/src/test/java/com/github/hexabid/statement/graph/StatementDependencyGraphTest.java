package com.github.hexabid.statement.graph;

import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementDependency;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StatementDependencyGraphTest {

    @Test
    void topologicalSortReturnsCorrectOrder() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.PAYMENT_READINESS))
                .addEdge(StatementDependency.requires(StatementCode.PAYMENT_READINESS, StatementCode.TERMS_ACCEPTANCE))
                .build();

        List<StatementCode> sorted = graph.topologicalSort();

        assertEquals(4, sorted.size());
        assertTrue(sorted.indexOf(StatementCode.LEGAL_CAPACITY) < sorted.indexOf(StatementCode.SANCTIONS_CLEARANCE));
        assertTrue(sorted.indexOf(StatementCode.SANCTIONS_CLEARANCE) < sorted.indexOf(StatementCode.PAYMENT_READINESS));
        assertTrue(sorted.indexOf(StatementCode.PAYMENT_READINESS) < sorted.indexOf(StatementCode.TERMS_ACCEPTANCE));
    }

    @Test
    void detectNoCycle() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .build();

        assertFalse(graph.hasCycle());
    }

    @Test
    void detectCycle() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.LEGAL_CAPACITY))
                .build();

        assertTrue(graph.hasCycle());
    }

    @Test
    void reachableFromReturnsTransitiveDependents() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.SOURCE_OF_FUNDS))
                .addEdge(StatementDependency.requires(StatementCode.SOURCE_OF_FUNDS, StatementCode.PAYMENT_READINESS))
                .addEdge(StatementDependency.requires(StatementCode.PAYMENT_READINESS, StatementCode.TERMS_ACCEPTANCE))
                .build();

        Set<StatementCode> reachable = graph.reachableFrom(StatementCode.SANCTIONS_CLEARANCE);

        assertEquals(3, reachable.size());
        assertTrue(reachable.contains(StatementCode.SOURCE_OF_FUNDS));
        assertTrue(reachable.contains(StatementCode.PAYMENT_READINESS));
        assertTrue(reachable.contains(StatementCode.TERMS_ACCEPTANCE));
    }

    @Test
    void rootNodesReturnsNodesWithNoPrerequisites() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.TERMS_ACCEPTANCE))
                .build();

        Set<StatementCode> roots = graph.rootNodes();

        assertEquals(1, roots.size());
        assertTrue(roots.contains(StatementCode.LEGAL_CAPACITY));
    }

    @Test
    void availableStatementsReturnsNodesWithAllPrereqsCompleted() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.TERMS_ACCEPTANCE))
                .build();

        Set<StatementCode> available = graph.availableStatements(Set.of(StatementCode.LEGAL_CAPACITY));

        assertEquals(1, available.size());
        assertTrue(available.contains(StatementCode.SANCTIONS_CLEARANCE));
    }

    @Test
    void isReachableNowReturnsTrueWhenAllPrereqsCompleted() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .build();

        assertTrue(graph.isReachableNow(StatementCode.SANCTIONS_CLEARANCE, Set.of(StatementCode.LEGAL_CAPACITY)));
        assertFalse(graph.isReachableNow(StatementCode.SANCTIONS_CLEARANCE, Set.of()));
    }

    @Test
    void prerequisitesOfReturnsCorrectSet() {
        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .build();

        Set<StatementCode> prereqs = graph.prerequisitesOf(StatementCode.SANCTIONS_CLEARANCE);

        assertEquals(1, prereqs.size());
        assertTrue(prereqs.contains(StatementCode.LEGAL_CAPACITY));
    }
}
