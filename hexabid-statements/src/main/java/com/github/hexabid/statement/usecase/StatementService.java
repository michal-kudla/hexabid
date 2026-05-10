package com.github.hexabid.statement.usecase;

import com.github.hexabid.statement.graph.StatementDependencyGraph;
import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.ParticipationDecision;
import com.github.hexabid.statement.model.ParticipationPolicyTemplateId;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.StatementAnswer;
import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementDefinition;
import com.github.hexabid.statement.model.StatementProgramInstance;
import com.github.hexabid.statement.model.StatementProgramInstanceId;
import com.github.hexabid.statement.model.StatementStep;
import com.github.hexabid.statement.model.StatementViolationType;
import com.github.hexabid.statement.policy.ParticipationPolicyEvaluator;
import com.github.hexabid.statement.port.in.GetParticipationDecisionQuery;
import com.github.hexabid.statement.port.in.GetStatementProgramQuery;
import com.github.hexabid.statement.port.in.ParticipationDecisionView;
import com.github.hexabid.statement.port.in.StartStatementProgramCommand;
import com.github.hexabid.statement.port.in.StatementProgramView;
import com.github.hexabid.statement.port.in.StatementStepView;
import com.github.hexabid.statement.port.in.StartStatementProgramUseCase;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerCommand;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerResult;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerUseCase;
import com.github.hexabid.statement.port.in.GetStatementProgramUseCase;
import com.github.hexabid.statement.port.in.GetParticipationDecisionUseCase;
import com.github.hexabid.statement.port.out.StatementProgramInstanceRepository;
import com.github.hexabid.statement.template.ParticipationPolicyTemplate;
import com.github.hexabid.statement.template.PolicyTemplateCatalog;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service implementing all four statement participation use cases.
 *
 * <p>Orchestrates domain logic (policy evaluation, graph traversal), repository
 * access, template lookup, and view mapping. The four inbound ports are implemented
 * by this single service; the Interface Segregation Principle is satisfied at the
 * port level (each adapter depends only on the port it needs).
 *
 * @see StartStatementProgramUseCase
 * @see SubmitStatementAnswerUseCase
 * @see GetStatementProgramUseCase
 * @see GetParticipationDecisionUseCase
 */
public final class StatementService implements
        StartStatementProgramUseCase,
        SubmitStatementAnswerUseCase,
        GetStatementProgramUseCase,
        GetParticipationDecisionUseCase {

    private final StatementProgramInstanceRepository repository;
    private final Clock clock;
    private final Map<String, ParticipationPolicyTemplate> templatesByName;
    private final Map<ParticipationPolicyTemplateId, ParticipationPolicyTemplate> templatesById;
    private final ParticipationPolicyEvaluator evaluator;

    public StatementService(StatementProgramInstanceRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.evaluator = new ParticipationPolicyEvaluator(clock);
        this.templatesByName = Map.of(
                "PUBLIC_CONSUMER_LIGHT_V1", PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1,
                "REGULATED_ASSET_BUYER_V1", PolicyTemplateCatalog.REGULATED_ASSET_BUYER_V1,
                "HIGH_VALUE_TENDER_V1", PolicyTemplateCatalog.HIGH_VALUE_TENDER_V1
        );
        this.templatesById = templatesByName.values().stream()
                .collect(Collectors.toMap(t -> t.id(), Function.identity()));
    }

    @Override
    public StatementProgramView startProgram(StartStatementProgramCommand command) {
        AuctionId auctionId = AuctionId.of(command.auctionId());
        PartyId candidateId = PartyId.of(command.candidateId());

        Optional<StatementProgramInstance> existing = repository.findByAuctionIdAndCandidateId(auctionId, candidateId);
        if (existing.isPresent()) {
            return toView(existing.get());
        }

        ParticipationPolicyTemplate template = templatesByName.get(command.templateName());
        if (template == null) {
            throw new IllegalArgumentException("Unknown policy template: " + command.templateName());
        }

        StatementProgramInstance instance = StatementProgramInstance.create(
                auctionId, candidateId, template.id(), template.version(), Instant.now(clock)
        );

        return toView(repository.save(instance));
    }

    @Override
    public SubmitStatementAnswerResult submitAnswer(SubmitStatementAnswerCommand command) {
        AuctionId auctionId = AuctionId.of(command.auctionId());
        PartyId candidateId = PartyId.of(command.candidateId());

        StatementProgramInstance instance = repository.findByAuctionIdAndCandidateId(auctionId, candidateId)
                .orElseThrow(() -> new IllegalStateException("No program instance found for auction " + auctionId + " and candidate " + candidateId));

        ParticipationPolicyTemplate template = templatesById.get(instance.templateId());
        if (template == null) {
            throw new IllegalStateException("Template not found for instance: " + instance.templateId());
        }

        StatementCode statementCode = new StatementCode(command.statementCode());
        Set<StatementCode> completedKeys = instance.answers().keySet();

        if (!template.graph().isReachableNow(statementCode, completedKeys)) {
            Set<StatementCode> prereqs = template.graph().prerequisitesOf(statementCode);
            List<String> missing = prereqs.stream()
                    .filter(p -> !completedKeys.contains(p))
                    .map(StatementCode::value)
                    .toList();
            return new SubmitStatementAnswerResult.PrerequisiteNotMet(statementCode.value(), missing);
        }

        StatementDefinition definition = template.statements().stream()
                .filter(sd -> sd.code().equals(statementCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown statement: " + statementCode));

        boolean disqualifying = definition.isDisqualifyingAnswer(command.answerValue());

        StatementAnswer answer = StatementAnswer.create(
                instance.id(), statementCode, command.answerValue(), disqualifying, Instant.now(clock)
        );

        instance.submitAnswer(answer);

        if (disqualifying) {
            Set<StatementCode> cascaded = template.graph().reachableFrom(statementCode);
            ParticipationDecision.Rejected rejected = new ParticipationDecision.Rejected(
                    candidateId, auctionId, statementCode, StatementViolationType.FATAL_DECLARATION,
                    new ArrayList<>(cascaded),
                    "Odrzucono z powodu oświadczenia: " + definition.title(),
                    Instant.now(clock)
            );
            instance.markRejected(rejected);
            repository.save(instance);
            return new SubmitStatementAnswerResult.AnswerRejected(toView(instance), rejected.humanReason());
        }

        ParticipationPolicyEvaluator.EvaluationResult evaluation = evaluator.evaluate(template, instance.answers());
        if (evaluation.status() == ParticipationPolicyEvaluator.EvaluationStatus.ADMITTED) {
            ParticipationDecision decision = new ParticipationDecision.Admitted(candidateId, auctionId, Instant.now(clock));
            instance.markCompleted(decision);
        } else if (evaluation.status() == ParticipationPolicyEvaluator.EvaluationStatus.PENDING) {
            ParticipationDecision decision = new ParticipationDecision.Pending(
                    candidateId, auctionId, evaluation.missingStatements(), List.of()
            );
            instance.markCompleted(decision);
        }

        repository.save(instance);
        return new SubmitStatementAnswerResult.AnswerAccepted(toView(instance));
    }

    @Override
    public StatementProgramView getProgram(GetStatementProgramQuery query) {
        AuctionId auctionId = AuctionId.of(query.auctionId());
        PartyId candidateId = PartyId.of(query.candidateId());

        StatementProgramInstance instance = repository.findByAuctionIdAndCandidateId(auctionId, candidateId)
                .orElseThrow(() -> new IllegalStateException("No program instance found"));

        return toView(instance);
    }

    @Override
    public ParticipationDecisionView getDecision(GetParticipationDecisionQuery query) {
        AuctionId auctionId = AuctionId.of(query.auctionId());
        PartyId candidateId = PartyId.of(query.candidateId());

        StatementProgramInstance instance = repository.findByAuctionIdAndCandidateId(auctionId, candidateId)
                .orElseThrow(() -> new IllegalStateException("No program instance found"));

        return toDecisionView(instance.decision());
    }

    private StatementProgramView toView(StatementProgramInstance instance) {
        ParticipationPolicyTemplate template = templatesById.get(instance.templateId());
        if (template == null) {
            throw new IllegalStateException("Template not found for instance: " + instance.templateId());
        }

        Set<StatementCode> completedKeys = instance.answers().keySet();
        Set<StatementCode> available = template.graph().availableStatements(completedKeys);

        Map<StatementCode, StatementDefinition> definitionMap = template.statements().stream()
                .collect(Collectors.toMap(StatementDefinition::code, Function.identity()));

        Map<StatementCode, Integer> stepOrderMap = template.steps().stream()
                .collect(Collectors.toMap(StatementStep::statementCode, StatementStep::order, (a, b) -> a));

        List<StatementStepView> availableViews = available.stream()
                .map(code -> toStepView(code, definitionMap.get(code), stepOrderMap, instance.answers(), template))
                .toList();

        List<StatementStepView> completedViews = completedKeys.stream()
                .map(code -> toStepView(code, definitionMap.get(code), stepOrderMap, instance.answers(), template))
                .toList();

        List<StatementStepView> blockedViews = template.graph().nodes().stream()
                .filter(code -> !completedKeys.contains(code) && !available.contains(code))
                .map(code -> toStepView(code, definitionMap.get(code), stepOrderMap, instance.answers(), template))
                .toList();

        return new StatementProgramView(
                instance.id().value(),
                instance.auctionId().value(),
                instance.candidateId().value(),
                template.name(),
                template.version().value(),
                instance.status().name(),
                availableViews,
                completedViews,
                blockedViews,
                toDecisionView(instance.decision())
        );
    }

    private StatementStepView toStepView(StatementCode code, StatementDefinition def,
                                          Map<StatementCode, Integer> stepOrderMap,
                                          Map<StatementCode, StatementAnswer> answers,
                                          ParticipationPolicyTemplate template) {
        String stepLabel = template.steps().stream()
                .filter(s -> s.statementCode().equals(code))
                .map(StatementStep::label)
                .findFirst()
                .orElse("");

        return new StatementStepView(
                code.value(),
                def != null ? def.title() : code.value(),
                def != null ? def.question() : "",
                def != null ? def.answerType().name() : "YES_NO",
                stepOrderMap.getOrDefault(code, 0),
                stepLabel,
                answers.containsKey(code) ? answers.get(code).answerValue() : null
        );
    }

    private ParticipationDecisionView toDecisionView(ParticipationDecision decision) {
        if (decision == null) {
            return new ParticipationDecisionView("PENDING", null, null, List.of(), List.of(), List.of());
        }

        return switch (decision) {
            case ParticipationDecision.Admitted a -> new ParticipationDecisionView(
                    "ADMITTED", null, null, List.of(), List.of(), List.of());
            case ParticipationDecision.AdmittedWithConditions awc -> new ParticipationDecisionView(
                    "ADMITTED_WITH_CONDITIONS", null, null, List.of(), List.of(), awc.conditions());
            case ParticipationDecision.Pending p -> new ParticipationDecisionView(
                    "PENDING", null, null,
                    p.missingStatements().stream().map(StatementCode::value).toList(),
                    List.of(), List.of());
            case ParticipationDecision.Rejected r -> new ParticipationDecisionView(
                    "REJECTED", r.rootCause().value(), r.humanReason(), List.of(),
                    r.cascadedStatements().stream().map(StatementCode::value).toList(), List.of());
        };
    }
}
