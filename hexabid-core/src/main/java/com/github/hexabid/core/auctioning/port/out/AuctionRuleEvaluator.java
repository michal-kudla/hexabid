package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.party.model.PartyId;

import java.util.List;

public interface AuctionRuleEvaluator {

    List<RuleViolation> evaluateParticipationRules(AuctionId auctionId, PartyId partyId);

    List<RuleViolation> evaluateBiddingRules(AuctionId auctionId, PartyId bidderId);

    List<RuleViolation> evaluateSettlementRules(AuctionId auctionId);

    boolean hasBlockingViolations(AuctionId auctionId, PartyId partyId, String phase);

    record RuleViolation(
            String ruleName,
            String message,
            boolean blocking,
            String requiredAction,
            String status,
            String severity
    ) {
        public RuleViolation {
            if (ruleName == null || message == null) {
                throw new IllegalArgumentException("ruleName and message must not be null");
            }
            if (status == null) {
                status = "VIOLATED";
            }
            if (severity == null) {
                severity = "BLOCKING";
            }
        }
    }
}
