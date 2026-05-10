package com.github.hexabid.statement.template;

import com.github.hexabid.statement.graph.StatementDependencyGraph;
import com.github.hexabid.statement.model.ParticipationPolicyTemplateId;
import com.github.hexabid.statement.model.PolicyTemplateVersion;
import com.github.hexabid.statement.model.StatementDefinition;
import com.github.hexabid.statement.model.StatementStep;

import java.util.List;
import java.util.Objects;

/**
 * A versioned template that defines the complete set of statements a candidate
 * must answer to qualify for auction participation.
 *
 * <p>A template bundles together the statement definitions, their dependency graph,
 * and the ordered steps for presentation. Templates are immutable once created
 * and are versioned to allow controlled evolution of participation requirements.
 *
 * @param id          the unique template identifier
 * @param name        the template name (e.g. "PUBLIC_CONSUMER_LIGHT_V1")
 * @param version     the template version
 * @param statements  the list of statement definitions that make up this template
 * @param graph       the dependency graph governing statement answer order
 * @param steps       the ordered presentation steps for the candidate workflow
 */
public record ParticipationPolicyTemplate(
        ParticipationPolicyTemplateId id,
        String name,
        PolicyTemplateVersion version,
        List<StatementDefinition> statements,
        StatementDependencyGraph graph,
        List<StatementStep> steps
) {

    public ParticipationPolicyTemplate {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(version, "version must not be null");
        statements = List.copyOf(Objects.requireNonNull(statements));
        Objects.requireNonNull(graph, "graph must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps));
    }
}
