package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * A directed dependency between two statements in a participation policy.
 *
 * <p>Dependencies define the order and conditions under which statements
 * must be answered — for example, a candidate cannot declare sanctions
 * clearance without first confirming legal capacity.
 *
 * @param from the statement that imposes the dependency (source)
 * @param to   the statement that is depended upon (target)
 * @param kind the nature of the dependency
 */
public record StatementDependency(
        StatementCode from,
        StatementCode to,
        DependencyKind kind
) {

    public StatementDependency {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
    }

    /**
     * Creates a hard dependency: {@code from} requires {@code to} to be answered first.
     *
     * @param from the dependent statement
     * @param to   the prerequisite statement
     * @return a dependency of kind {@link DependencyKind#REQUIRES}
     */
    public static StatementDependency requires(StatementCode from, StatementCode to) {
        return new StatementDependency(from, to, DependencyKind.REQUIRES);
    }

    /**
     * Creates a soft dependency: {@code from} is conditionally linked to {@code to}.
     *
     * @param from the dependent statement
     * @param to   the conditionally related statement
     * @return a dependency of kind {@link DependencyKind#CONDITIONAL}
     */
    public static StatementDependency conditional(StatementCode from, StatementCode to) {
        return new StatementDependency(from, to, DependencyKind.CONDITIONAL);
    }

    /**
     * The nature of a dependency between two statements.
     */
    public enum DependencyKind {

        /** The source statement requires the target to be answered first. */
        REQUIRES,

        /** The source statement is conditionally related to the target. */
        CONDITIONAL
    }
}
