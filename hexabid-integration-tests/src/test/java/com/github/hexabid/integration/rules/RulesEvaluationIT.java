package com.github.hexabid.integration.rules;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rules Evaluation & Document Submission")
class RulesEvaluationIT extends IntegrationTestBase {

    private static CreateAuctionRequest rulesAuctionRequest(String title) {
        CreateAuctionRequest req = new CreateAuctionRequest();
        req.setTitle(unique(title));
        req.setStartingPrice(new Money().amount("5000.00").currency("PLN"));
        req.setEndsAt(OffsetDateTime.now().plusDays(7));
        return req;
    }

    @Nested
    @DisplayName("R1 - Rule evaluation for an auction")
    class R1_EvaluateRules {

        @Test
        @DisplayName("R1.1 - Evaluate all rules for an auction returns evaluations per phase")
        void shouldEvaluateAllRulesForAuction() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules eval auction"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, null
            );

            assertThat(evaluation).isNotNull();
            assertThat(evaluation.getAuctionId()).isEqualTo(auction.getAuctionId());
            assertThat(evaluation.getEvaluations()).isNotEmpty();
            assertThat(evaluation.getEvaluations()).anyMatch(e -> e.getPhase() == RulePhase.PARTICIPATION);
            assertThat(evaluation.getEvaluations()).anyMatch(e -> e.getPhase() == RulePhase.BIDDING);
            assertThat(evaluation.getEvaluations()).anyMatch(e -> e.getPhase() == RulePhase.SETTLEMENT);
        }

        @Test
        @DisplayName("R1.2 - Evaluate rules filtered by PARTICIPATION phase only")
        void shouldFilterByParticipationPhase() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules participation"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, RulePhase.PARTICIPATION
            );

            assertThat(evaluation.getEvaluations()).hasSize(1);
            assertThat(evaluation.getEvaluations().getFirst().getPhase()).isEqualTo(RulePhase.PARTICIPATION);
        }

        @Test
        @DisplayName("R1.3 - Evaluate rules filtered by BIDDING phase only")
        void shouldFilterByBiddingPhase() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules bidding"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, RulePhase.BIDDING
            );

            assertThat(evaluation.getEvaluations()).hasSize(1);
            assertThat(evaluation.getEvaluations().getFirst().getPhase()).isEqualTo(RulePhase.BIDDING);
        }

        @Test
        @DisplayName("R1.4 - Evaluate rules filtered by SETTLEMENT phase only")
        void shouldFilterBySettlementPhase() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules settlement"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, RulePhase.SETTLEMENT
            );

            assertThat(evaluation.getEvaluations()).hasSize(1);
            assertThat(evaluation.getEvaluations().getFirst().getPhase()).isEqualTo(RulePhase.SETTLEMENT);
        }

        @Test
        @DisplayName("R1.5 - Each rule violation has required fields")
        void eachViolationHasRequiredFields() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules fields check"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, null
            );

            for (RulePhaseEvaluation phaseEval : evaluation.getEvaluations()) {
                for (RuleViolationItem rule : phaseEval.getRules()) {
                    assertThat(rule.getRuleName()).isNotBlank();
                    assertThat(rule.getMessage()).isNotNull();
                    assertThat(rule.getBlocking()).isNotNull();
                    assertThat(rule.getRequiredAction()).isNotNull();
                    assertThat(rule.getStatus()).isNotNull();
                    assertThat(rule.getSeverity()).isNotNull();
                }
            }
        }

        @Test
        @DisplayName("R1.6 - Settlement phase has FULL_PAYMENT_SETTLEMENT rule")
        void settlementPhaseHasFullPaymentRule() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules settlement full payment"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, RulePhase.SETTLEMENT
            );

            var settlementRules = evaluation.getEvaluations().getFirst().getRules();
            assertThat(settlementRules).anyMatch(r -> r.getRuleName().equals("FULL_PAYMENT_SETTLEMENT"));
        }

        @Test
        @DisplayName("R1.7 - Bidding phase has KYC_VERIFIED_FOR_BIDDING rule")
        void biddingPhaseHasKycRule() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules bidding kyc"), API_VERSION);

            RuleEvaluationResponse evaluation = buyerAuctionsApi.evaluateAuctionRules(
                    auction.getAuctionId(), API_VERSION, RulePhase.BIDDING
            );

            var biddingRules = evaluation.getEvaluations().getFirst().getRules();
            assertThat(biddingRules).anyMatch(r -> r.getRuleName().equals("KYC_VERIFIED_FOR_BIDDING"));
        }
    }

    @Nested
    @DisplayName("R2 - Document submission")
    class R2_SubmitDocument {

        @Test
        @DisplayName("R2.1 - Submit excise certificate copy")
        void shouldSubmitExciseCertificateCopy() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Doc submit excise"), API_VERSION);

            var request = new SubmitDocumentRequest();
            request.setDocumentType(DocumentType.EXCISE_CERTIFICATE);
            request.setStatus(DocumentStatus.COPY);

            var response = buyerAuctionsApi.submitDocument(auction.getAuctionId(), request, API_VERSION);

            assertThat(response).isNotNull();
            assertThat(response.getDocumentType()).isEqualTo(DocumentType.EXCISE_CERTIFICATE);
            assertThat(response.getStatus()).isEqualTo(DocumentStatus.COPY);
        }

        @Test
        @DisplayName("R2.2 - Submit customs exemption original")
        void shouldSubmitCustomsExemptionOriginal() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Doc submit customs"), API_VERSION);

            var request = new SubmitDocumentRequest();
            request.setDocumentType(DocumentType.CUSTOMS_EXEMPTION);
            request.setStatus(DocumentStatus.ORIGINAL);

            var response = buyerAuctionsApi.submitDocument(auction.getAuctionId(), request, API_VERSION);

            assertThat(response).isNotNull();
            assertThat(response.getDocumentType()).isEqualTo(DocumentType.CUSTOMS_EXEMPTION);
            assertThat(response.getStatus()).isEqualTo(DocumentStatus.ORIGINAL);
        }
    }

    @Nested
    @DisplayName("R3 - Rules evaluation security")
    class R3_RulesSecurity {

        @Test
        @DisplayName("R3.1 - Unauthenticated rule evaluation returns 401")
        void shouldRejectUnauthenticatedRuleEvaluation() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Rules security"), API_VERSION);

            ApiClient anonymousClient = anonymousApiClient();
            AuctionsApi anonymousApi = new AuctionsApi(anonymousClient);

            try {
                anonymousApi.evaluateAuctionRules(auction.getAuctionId(), API_VERSION, null);
            } catch (com.github.hexabid.contract.client.ApiException e) {
                assertThat(e.getCode()).isEqualTo(401);
            }
        }

        @Test
        @DisplayName("R3.2 - Unauthenticated document submission returns 401")
        void shouldRejectUnauthenticatedDocumentSubmission() throws Exception {
            var auction = sellerAuctionsApi.createAuction(rulesAuctionRequest("Doc security"), API_VERSION);

            ApiClient anonymousClient = anonymousApiClient();
            AuctionsApi anonymousApi = new AuctionsApi(anonymousClient);

            var request = new SubmitDocumentRequest();
            request.setDocumentType(DocumentType.EXCISE_CERTIFICATE);
            request.setStatus(DocumentStatus.COPY);

            try {
                anonymousApi.submitDocument(auction.getAuctionId(), request, API_VERSION);
            } catch (com.github.hexabid.contract.client.ApiException e) {
                assertThat(e.getCode()).isEqualTo(401);
            }
        }
    }
}
