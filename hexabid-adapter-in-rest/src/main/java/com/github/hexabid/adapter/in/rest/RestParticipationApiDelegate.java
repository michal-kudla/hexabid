package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.contract.api.ParticipationApiDelegate;
import com.github.hexabid.contract.model.ParticipationDecisionView;
import com.github.hexabid.contract.model.StartParticipationProgramRequest;
import com.github.hexabid.contract.model.StatementProgramView;
import com.github.hexabid.contract.model.SubmitStatementAnswerRequest;
import com.github.hexabid.contract.model.SubmitStatementAnswerResponse;
import com.github.hexabid.statement.port.in.GetParticipationDecisionQuery;
import com.github.hexabid.statement.port.in.GetStatementProgramQuery;
import com.github.hexabid.statement.port.in.StartStatementProgramCommand;
import com.github.hexabid.statement.port.in.StartStatementProgramUseCase;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerCommand;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerResult;
import com.github.hexabid.statement.port.in.SubmitStatementAnswerUseCase;
import com.github.hexabid.statement.port.in.GetStatementProgramUseCase;
import com.github.hexabid.statement.port.in.GetParticipationDecisionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * REST inbound adapter for the Participation API.
 *
 * <p>Implements the OpenAPI-generated {@link ParticipationApiDelegate} interface and translates
 * incoming HTTP requests into domain use-case invocations. Responsible for authentication
 * checks, request-to-command mapping, and domain-to-contract view conversion.</p>
 */
@Service
public class RestParticipationApiDelegate implements ParticipationApiDelegate {

    private final StartStatementProgramUseCase startStatementProgramUseCase;
    private final SubmitStatementAnswerUseCase submitStatementAnswerUseCase;
    private final GetStatementProgramUseCase getStatementProgramUseCase;
    private final GetParticipationDecisionUseCase getParticipationDecisionUseCase;
    private final CurrentUserProvider currentUserProvider;

    public RestParticipationApiDelegate(
            StartStatementProgramUseCase startStatementProgramUseCase,
            SubmitStatementAnswerUseCase submitStatementAnswerUseCase,
            GetStatementProgramUseCase getStatementProgramUseCase,
            GetParticipationDecisionUseCase getParticipationDecisionUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.startStatementProgramUseCase = startStatementProgramUseCase;
        this.submitStatementAnswerUseCase = submitStatementAnswerUseCase;
        this.getStatementProgramUseCase = getStatementProgramUseCase;
        this.getParticipationDecisionUseCase = getParticipationDecisionUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Retrieves the statement program instance for the authenticated user's participation in the given auction.
     *
     * <p>Returns {@code 401 UNAUTHORIZED} if no authenticated user is present,
     * {@code 404 NOT FOUND} if no program instance exists, or {@code 200 OK} with the program view.</p>
     *
     * @param auctionId   the auction identifier
     * @param xApiVersion the API version header value
     * @return response entity containing the statement program view
     */
    @Override
    public ResponseEntity<StatementProgramView> getParticipationProgram(UUID auctionId, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            com.github.hexabid.statement.port.in.StatementProgramView view =
                    getStatementProgramUseCase.getProgram(new GetStatementProgramQuery(auctionId, authenticatedUser.partyId().value()));
            return ResponseEntity.ok(toContractView(view));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Starts a new statement program participation for the authenticated user in the given auction.
     *
     * <p>Delegates to {@link StartStatementProgramUseCase} and returns {@code 201 CREATED}
     * with the created program view, or {@code 401 UNAUTHORIZED} if unauthenticated.</p>
     *
     * @param auctionId                        the auction identifier
     * @param startParticipationProgramRequest the request containing the template name
     * @param xApiVersion                      the API version header value
     * @return response entity containing the created statement program view
     */
    @Override
    public ResponseEntity<StatementProgramView> startParticipationProgram(
            UUID auctionId, StartParticipationProgramRequest startParticipationProgramRequest, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        com.github.hexabid.statement.port.in.StatementProgramView view =
                startStatementProgramUseCase.startProgram(new StartStatementProgramCommand(
                        auctionId,
                        authenticatedUser.partyId().value(),
                        startParticipationProgramRequest.getTemplateName()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toContractView(view));
    }

    /**
     * Submits an answer to a specific statement within the user's participation program.
     *
     * <p>Maps the result of {@link SubmitStatementAnswerUseCase} to the contract response:
     * accepted, rejected, or prerequisite-not-met (returns {@code 400 BAD REQUEST}).</p>
     *
     * @param auctionId                      the auction identifier
     * @param statementCode                  the code of the statement being answered
     * @param submitStatementAnswerRequest   the request containing the answer value
     * @param xApiVersion                    the API version header value
     * @return response entity containing the submission result
     */
    @Override
    public ResponseEntity<SubmitStatementAnswerResponse> submitStatementAnswer(
            UUID auctionId, String statementCode, SubmitStatementAnswerRequest submitStatementAnswerRequest, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SubmitStatementAnswerResult result = submitStatementAnswerUseCase.submitAnswer(
                new SubmitStatementAnswerCommand(
                        auctionId,
                        authenticatedUser.partyId().value(),
                        statementCode,
                        submitStatementAnswerRequest.getAnswerValue()
                )
        );

        SubmitStatementAnswerResponse response = new SubmitStatementAnswerResponse();

        if (result instanceof SubmitStatementAnswerResult.AnswerAccepted accepted) {
            response.setResultType(SubmitStatementAnswerResponse.ResultTypeEnum.ACCEPTED);
            response.setProgram(toContractView(accepted.programView()));
        } else if (result instanceof SubmitStatementAnswerResult.AnswerRejected rejected) {
            response.setResultType(SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);
            response.setReason(rejected.reason());
            response.setProgram(toContractView(rejected.programView()));
        } else if (result instanceof SubmitStatementAnswerResult.PrerequisiteNotMet pnm) {
            response.setResultType(SubmitStatementAnswerResponse.ResultTypeEnum.PREREQUISITE_NOT_MET);
            response.setMissingPrerequisites(pnm.missingPrerequisites());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the participation decision for the authenticated user in the given auction.
     *
     * <p>Returns {@code 401 UNAUTHORIZED} if unauthenticated, {@code 404 NOT FOUND}
     * if no decision exists, or {@code 200 OK} with the decision view.</p>
     *
     * @param auctionId   the auction identifier
     * @param xApiVersion the API version header value
     * @return response entity containing the participation decision view
     */
    @Override
    public ResponseEntity<ParticipationDecisionView> getParticipationDecision(UUID auctionId, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            com.github.hexabid.statement.port.in.ParticipationDecisionView view =
                    getParticipationDecisionUseCase.getDecision(new GetParticipationDecisionQuery(
                            auctionId, authenticatedUser.partyId().value()));
            return ResponseEntity.ok(toContractDecisionView(view));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private StatementProgramView toContractView(com.github.hexabid.statement.port.in.StatementProgramView domain) {
        StatementProgramView view = new StatementProgramView();
        view.setProgramInstanceId(domain.programInstanceId());
        view.setAuctionId(domain.auctionId());
        view.setCandidateId(domain.candidateId());
        view.setTemplateName(domain.templateName());
        view.setTemplateVersion(domain.templateVersion());
        view.setStatus(StatementProgramView.StatusEnum.valueOf(domain.status()));
        view.setAvailableStatements(domain.availableStatements().stream().map(this::toContractStepView).toList());
        view.setCompletedStatements(domain.completedStatements().stream().map(this::toContractStepView).toList());
        view.setBlockedStatements(domain.blockedStatements().stream().map(this::toContractStepView).toList());
        if (domain.decision() != null) {
            view.setDecision(toContractDecisionView(domain.decision()));
        }
        return view;
    }

    private com.github.hexabid.contract.model.StatementStepView toContractStepView(
            com.github.hexabid.statement.port.in.StatementStepView domain) {
        com.github.hexabid.contract.model.StatementStepView view = new com.github.hexabid.contract.model.StatementStepView();
        view.setStatementCode(domain.statementCode());
        view.setTitle(domain.title());
        view.setQuestion(domain.question());
        view.setAnswerType(com.github.hexabid.contract.model.StatementStepView.AnswerTypeEnum.valueOf(domain.answerType()));
        view.setOrder(domain.order());
        view.setStepLabel(domain.stepLabel());
        view.setAnswerValue(domain.answerValue());
        return view;
    }

    private ParticipationDecisionView toContractDecisionView(
            com.github.hexabid.statement.port.in.ParticipationDecisionView domain) {
        ParticipationDecisionView view = new ParticipationDecisionView();
        view.setStatus(ParticipationDecisionView.StatusEnum.valueOf(domain.status()));
        view.setRootCause(domain.rootCause());
        view.setHumanReason(domain.humanReason());
        view.setMissingStatements(domain.missingStatements());
        view.setCascadedStatements(domain.cascadedStatements());
        view.setConditions(domain.conditions());
        return view;
    }
}
