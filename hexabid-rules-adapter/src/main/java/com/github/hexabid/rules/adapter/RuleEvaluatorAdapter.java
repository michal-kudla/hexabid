package com.github.hexabid.rules.adapter;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;
import com.github.hexabid.core.auctioning.port.out.AuctionRuleEvaluator;
import com.github.hexabid.core.auctioning.port.out.DocumentRepository;
import com.github.hexabid.core.auctioning.port.out.WadiumDepositPort;
import com.github.hexabid.core.party.model.PartyId;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.engine.RuleCatalog;
import com.github.hexabid.rules.engine.RuleEngine;
import com.github.hexabid.rules.model.Pending;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RuleEvaluationResult;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.Satisfied;
import com.github.hexabid.rules.model.Violated;

import java.math.BigDecimal;
import java.util.List;

public class RuleEvaluatorAdapter implements AuctionRuleEvaluator {

    private final RuleEngine ruleEngine;
    private final DocumentRepository documentRepository;
    private final WadiumDepositPort wadiumDepositPort;

    public RuleEvaluatorAdapter(RuleEngine ruleEngine, DocumentRepository documentRepository, WadiumDepositPort wadiumDepositPort) {
        this.ruleEngine = ruleEngine;
        this.documentRepository = documentRepository;
        this.wadiumDepositPort = wadiumDepositPort;
    }

    @Override
    public List<RuleViolation> evaluateParticipationRules(AuctionId auctionId, PartyId partyId) {
        var context = buildParticipationContext(auctionId, partyId);
        return toViolations(ruleEngine.evaluateAll(RulePhase.PARTICIPATION, context));
    }

    @Override
    public List<RuleViolation> evaluateBiddingRules(AuctionId auctionId, PartyId bidderId) {
        var context = buildBiddingContext(auctionId, bidderId);
        return toViolations(ruleEngine.evaluateAll(RulePhase.BIDDING, context));
    }

    @Override
    public List<RuleViolation> evaluateSettlementRules(AuctionId auctionId) {
        var context = buildSettlementContext(auctionId);
        return toViolations(ruleEngine.evaluateAll(RulePhase.SETTLEMENT, context));
    }

    @Override
    public boolean hasBlockingViolations(AuctionId auctionId, PartyId partyId, String phase) {
        var rulePhase = RulePhase.valueOf(phase);
        var context = buildParticipationContext(auctionId, partyId);
        return ruleEngine.hasBlockingViolations(rulePhase, context);
    }

    private RuleContext buildParticipationContext(AuctionId auctionId, PartyId partyId) {
        var builder = RuleContext.builder();
        populateCommonAttributes(builder, auctionId, partyId);
        return builder.build();
    }

    private RuleContext buildBiddingContext(AuctionId auctionId, PartyId bidderId) {
        var builder = RuleContext.builder();
        populateCommonAttributes(builder, auctionId, bidderId);
        return builder.build();
    }

    private RuleContext buildSettlementContext(AuctionId auctionId) {
        return RuleContext.builder().build();
    }

    private void populateCommonAttributes(RuleContext.Builder builder, AuctionId auctionId, PartyId partyId) {
        builder.kycVerified(true);
        if (partyId != null) {
            builder.wadiumPaid(wadiumDepositPort.hasPaidWadium(partyId, auctionId));
            builder.exciseDocumentStatus(
                    documentRepository.getDocumentStatus(partyId, DocumentType.EXCISE_CERTIFICATE).name());
            builder.customsExemptionDocStatus(
                    documentRepository.getDocumentStatus(partyId, DocumentType.CUSTOMS_EXEMPTION).name());
        }
    }

    private List<RuleViolation> toViolations(List<RuleEvaluationResult> results) {
        return results.stream()
                .map(this::toViolation)
                .toList();
    }

    private RuleViolation toViolation(RuleEvaluationResult result) {
        return switch (result) {
            case Satisfied s -> new RuleViolation(s.ruleName().value(), "satisfied", false, "", "SATISFIED", s.severity().name());
            case Pending p -> new RuleViolation(p.ruleName().value(), p.requiredAction(), false, p.requiredAction(), "PENDING", p.severity().name());
            case Violated v -> new RuleViolation(v.ruleName().value(), v.violationMessage(), v.severity() == com.github.hexabid.rules.model.RuleSeverity.BLOCKING, "", "VIOLATED", v.severity().name());
        };
    }
}
