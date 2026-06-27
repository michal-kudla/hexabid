package com.github.hexabid.statement.graph;

import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Directed acyclic graph (DAG) modelling dependencies between statements.
 *
 * <p>An edge {@code A -> B} means "statement B can only be asked or evaluated
 * after statement A has been answered". The graph supports:
 * <ul>
 *   <li>Topological sorting for presentation order</li>
 *   <li>Reachability analysis for cascade cancellation after rejection</li>
 *   <li>Prerequisite checking to enforce answer ordering</li>
 *   <li>Cycle detection to prevent invalid configurations</li>
 * </ul>
 *
 * <p>Use {@link #builder()} to construct instances. The builder does not
 * enforce acyclicity — call {@link #hasCycle()} after construction to validate.
 *
 * @see StatementDependency
 * @see StatementCode
 */
public final class StatementDependencyGraph {

    private final Set<StatementCode> nodes;
    private final Map<StatementCode, Set<StatementCode>> adjacencyList;
    private final Map<StatementCode, Set<StatementCode>> reverseAdjacencyList;
    private final List<StatementDependency> edges;

    private StatementDependencyGraph(Set<StatementCode> nodes, List<StatementDependency> edges,
                                      Map<StatementCode, Set<StatementCode>> adjacencyList,
                                      Map<StatementCode, Set<StatementCode>> reverseAdjacencyList) {
        this.nodes = Set.copyOf(nodes);
        this.edges = List.copyOf(edges);
        this.adjacencyList = Collections.unmodifiableMap(
                copyAdjacencyMap(adjacencyList));
        this.reverseAdjacencyList = Collections.unmodifiableMap(
                copyAdjacencyMap(reverseAdjacencyList));
    }

    private static Map<StatementCode, Set<StatementCode>> copyAdjacencyMap(
            Map<StatementCode, Set<StatementCode>> source) {
        Map<StatementCode, Set<StatementCode>> copy = new HashMap<>();
        source.forEach((key, value) -> copy.put(key, Set.copyOf(value)));
        return copy;
    }

    /**
     * Returns a new builder for constructing a dependency graph.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns all statement codes in this graph.
     */
    public Set<StatementCode> nodes() { return nodes; }

    /**
     * Returns all dependency edges in this graph.
     */
    public List<StatementDependency> edges() { return edges; }

    /**
     * Returns the direct dependents of the given statement — i.e. statements
     * that depend on the given statement and can only be answered after it.
     */
    public Set<StatementCode> dependentsOf(StatementCode code) {
        return adjacencyList.getOrDefault(code, Set.of());
    }

    /**
     * Returns the direct prerequisites of the given statement — i.e. statements
     * that must be answered before this one can be attempted.
     */
    public Set<StatementCode> prerequisitesOf(StatementCode code) {
        return reverseAdjacencyList.getOrDefault(code, Set.of());
    }

    /**
     * Computes the transitive closure of all statements reachable from
     * the given source via forward edges (dependents).
     *
     * <p>The source statement itself is excluded from the result.
     * This is used to compute cascade cancellation after a rejection:
     * if a prerequisite is rejected, all dependently reachable statements
     * are no longer required.
     *
     * @param source the starting statement
     * @return all transitively dependent statements, excluding the source
     */
    public Set<StatementCode> reachableFrom(StatementCode source) {
        Set<StatementCode> visited = new LinkedHashSet<>();
        dfs(source, visited);
        visited.remove(source);
        return visited;
    }

    private void dfs(StatementCode current, Set<StatementCode> visited) {
        visited.add(current);
        for (StatementCode dependent : adjacencyList.getOrDefault(current, Set.of())) {
            if (!visited.contains(dependent)) {
                dfs(dependent, visited);
            }
        }
    }

    /**
     * Returns all statements that have no prerequisites — the entry points
     * of the dependency graph.
     */
    public Set<StatementCode> rootNodes() {
        return nodes.stream()
                .filter(node -> !reverseAdjacencyList.containsKey(node) || reverseAdjacencyList.get(node).isEmpty())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    }

    /**
     * Returns a topological ordering of all statements.
     *
     * <p>The ordering guarantees that every statement appears after all of
     * its prerequisites. This is suitable for determining the presentation
     * order to a candidate.
     *
     * @return statements in topological order
     * @throws IllegalStateException if the graph contains a cycle
     */
    public List<StatementCode> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Cannot perform topological sort on graph with cycles");
        }

        Map<StatementCode, Integer> inDegree = new HashMap<>();
        for (StatementCode node : nodes) {
            inDegree.put(node, reverseAdjacencyList.getOrDefault(node, Set.of()).size());
        }

        List<StatementCode> result = new ArrayList<>();
        Set<StatementCode> visited = new HashSet<>();

        for (StatementCode root : rootNodes()) {
            topologicalVisit(root, inDegree, result, visited);
        }

        return result;
    }

    private void topologicalVisit(StatementCode node, Map<StatementCode, Integer> inDegree,
                                   List<StatementCode> result, Set<StatementCode> visited) {
        if (visited.contains(node)) return;
        visited.add(node);
        result.add(node);

        for (StatementCode dependent : adjacencyList.getOrDefault(node, Set.of())) {
            int newDegree = inDegree.get(dependent) - 1;
            inDegree.put(dependent, newDegree);
            if (newDegree == 0) {
                topologicalVisit(dependent, inDegree, result, visited);
            }
        }
    }

    /**
     * Checks whether this graph contains a cycle.
     *
     * <p>A cyclic dependency graph is invalid because it would make some
     * statements unreachable (they would require themselves as prerequisites).
     *
     * @return {@code true} if a cycle exists
     */
    public boolean hasCycle() {
        Set<StatementCode> visited = new HashSet<>();
        Set<StatementCode> recursionStack = new HashSet<>();

        for (StatementCode node : rootNodes()) {
            if (hasCycleDfs(node, visited, recursionStack)) {
                return true;
            }
        }
        for (StatementCode node : nodes) {
            if (!visited.contains(node) && hasCycleDfs(node, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleDfs(StatementCode node, Set<StatementCode> visited, Set<StatementCode> recursionStack) {
        visited.add(node);
        recursionStack.add(node);

        for (StatementCode dependent : adjacencyList.getOrDefault(node, Set.of())) {
            if (!visited.contains(dependent)) {
                if (hasCycleDfs(dependent, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(dependent)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    /**
     * Returns the set of statements that are currently available for the candidate
     * to answer, given the set of already-completed statements.
     *
     * <p>A statement is available if all of its prerequisites have been completed.
     *
     * @param completedStatements the set of statement codes that have been answered
     * @return the set of currently answerable statements
     */
    public Set<StatementCode> availableStatements(Set<StatementCode> completedStatements) {
        Objects.requireNonNull(completedStatements, "completedStatements must not be null");
        Set<StatementCode> available = new LinkedHashSet<>();

        for (StatementCode node : nodes) {
            if (completedStatements.contains(node)) continue;
            Set<StatementCode> prereqs = prerequisitesOf(node);
            if (prereqs.isEmpty() || completedStatements.containsAll(prereqs)) {
                available.add(node);
            }
        }

        return available;
    }

    /**
     * Checks whether the given statement can be answered right now,
     * given the set of already-completed statements.
     *
     * @param code                 the statement to check
     * @param completedStatements the set of already-answered statement codes
     * @return {@code true} if all prerequisites are satisfied and the statement
     *         is not already completed
     */
    public boolean isReachableNow(StatementCode code, Set<StatementCode> completedStatements) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(completedStatements, "completedStatements must not be null");
        if (!nodes.contains(code)) return false;
        if (completedStatements.contains(code)) return false;
        Set<StatementCode> prereqs = prerequisitesOf(code);
        return prereqs.isEmpty() || completedStatements.containsAll(prereqs);
    }

    /**
     * Builder for constructing a {@link StatementDependencyGraph}.
     *
     * <p>Add nodes and edges, then call {@link #build()} to create the graph.
     * The builder does not enforce acyclicity — validate with
     * {@link StatementDependencyGraph#hasCycle()} after construction.
     */
    public static final class Builder {
        private final Set<StatementCode> nodes = new LinkedHashSet<>();
        private final List<StatementDependency> edges = new ArrayList<>();

        /**
         * Adds a node (statement code) to the graph.
         */
        public Builder addNode(StatementCode code) {
            nodes.add(code);
            return this;
        }

        /**
         * Adds a directed edge to the graph.
         *
         * <p>Both the source and target nodes are automatically added
         * to the graph if not already present.
         */
        public Builder addEdge(StatementDependency edge) {
            nodes.add(edge.from());
            nodes.add(edge.to());
            edges.add(edge);
            return this;
        }

        /**
         * Builds the dependency graph from the added nodes and edges.
         *
         * @return a new immutable dependency graph
         */
        public StatementDependencyGraph build() {
            Map<StatementCode, Set<StatementCode>> adjacencyList = new HashMap<>();
            Map<StatementCode, Set<StatementCode>> reverseAdjacencyList = new HashMap<>();

            for (StatementCode node : nodes) {
                adjacencyList.put(node, new LinkedHashSet<>());
                reverseAdjacencyList.put(node, new LinkedHashSet<>());
            }

            for (StatementDependency edge : edges) {
                adjacencyList.computeIfAbsent(edge.from(), k -> new LinkedHashSet<>()).add(edge.to());
                reverseAdjacencyList.computeIfAbsent(edge.to(), k -> new LinkedHashSet<>()).add(edge.from());
            }

            return new StatementDependencyGraph(nodes, edges, adjacencyList, reverseAdjacencyList);
        }
    }
}
