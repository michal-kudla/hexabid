package com.github.hexabid.integration.participation;

import com.github.hexabid.contract.client.ApiException;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatementCollectionFlowIT extends IntegrationTestBase {

    @Nested
    @DisplayName("SC1 — Start program and complete all statements — consumer light")
    class IT_SC1_ConsumerLightFullAdmission {

        @Test
        @DisplayName("Should admit candidate after completing all statements positively")
        void shouldAdmitCandidateAfterAllStatements() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            StatementProgramView program = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            assertThat(program.getStatus()).isEqualTo(StatementProgramView.StatusEnum.IN_PROGRESS);
            assertThat(program.getAvailableStatements()).isNotEmpty();
            assertThat(program.getTemplateName()).isEqualTo("PUBLIC_CONSUMER_LIGHT_V1");

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");

            List<String> toAnswer = new ArrayList<>(List.of(
                    "LEGAL_CAPACITY", "SANCTIONS_CLEARANCE", "PAYMENT_READINESS", "TERMS_ACCEPTANCE"));

            for (String statementCode : toAnswer) {
                SubmitStatementAnswerResponse answerResult = buyerParticipationApi.submitStatementAnswer(
                        auction.getAuctionId(), statementCode, yesAnswer, API_VERSION);

                assertThat(answerResult.getResultType())
                        .isIn(SubmitStatementAnswerResponse.ResultTypeEnum.ACCEPTED,
                                SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);
            }

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);

            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.ADMITTED);
        }
    }

    @Nested
    @DisplayName("SC2 — Disqualifying answer causes immediate rejection")
    class IT_SC2_ImmediateRejection {

        @Test
        @DisplayName("Should reject candidate after disqualifying answer on sanctions clearance")
        void shouldRejectAfterDisqualifyingAnswer() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            StatementProgramView program = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            assertThat(program.getAvailableStatements()).hasSize(1);
            assertThat(program.getAvailableStatements().get(0).getStatementCode()).isEqualTo("LEGAL_CAPACITY");

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);

            SubmitStatementAnswerRequest noAnswer = new SubmitStatementAnswerRequest();
            noAnswer.setAnswerValue("NO");
            SubmitStatementAnswerResponse rejectResult = buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "SANCTIONS_CLEARANCE", noAnswer, API_VERSION);

            assertThat(rejectResult.getResultType()).isEqualTo(SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);
            assertThat(rejectResult.getReason()).isNotBlank();

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);

            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.REJECTED);
            assertThat(decision.getRootCause()).isEqualTo("SANCTIONS_CLEARANCE");
            assertThat(decision.getCascadedStatements()).contains("PAYMENT_READINESS", "TERMS_ACCEPTANCE");
        }
    }

    @Nested
    @DisplayName("SC3 — Cannot answer statement before prerequisite is completed")
    class IT_SC3_PrerequisiteBlocking {

        @Test
        @DisplayName("Should reject answer when prerequisite not met")
        void shouldRejectAnswerWhenPrerequisiteNotMet() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");

            assertThatThrownBy(() -> buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "TERMS_ACCEPTANCE", yesAnswer, API_VERSION))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Nested
    @DisplayName("SC4 — Get program shows correct available and blocked statements")
    class IT_SC4_ProgramView {

        @Test
        @DisplayName("Should show available statements progressing as answers are submitted")
        void shouldShowProgressingAvailableStatements() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            StatementProgramView program = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            assertThat(program.getAvailableStatements()).hasSize(1);
            assertThat(program.getAvailableStatements().get(0).getStatementCode()).isEqualTo("LEGAL_CAPACITY");
            assertThat(program.getBlockedStatements()).isNotEmpty();

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);

            StatementProgramView updatedProgram = buyerParticipationApi.getParticipationProgram(
                    auction.getAuctionId(), API_VERSION);

            assertThat(updatedProgram.getAvailableStatements().stream()
                    .map(StatementStepView::getStatementCode).toList()).contains("SANCTIONS_CLEARANCE");
            assertThat(updatedProgram.getCompletedStatements().stream()
                    .map(StatementStepView::getStatementCode).toList()).contains("LEGAL_CAPACITY");
        }
    }

    @Nested
    @DisplayName("SC5 — Idempotent program start returns existing instance")
    class IT_SC5_IdempotentStart {

        @Test
        @DisplayName("Should return the same program when started twice for the same auction and candidate")
        void shouldReturnSameProgramOnDuplicateStart() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            StatementProgramView first = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);
            StatementProgramView second = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            assertThat(second.getProgramInstanceId()).isEqualTo(first.getProgramInstanceId());
            assertThat(second.getStatus()).isEqualTo(first.getStatus());
        }
    }

    @Nested
    @DisplayName("SC6 — Regulated asset buyer template with branching graph")
    class IT_SC6_RegulatedAssetBuyer {

        @Test
        @DisplayName("Should admit candidate through regulated asset template")
        void shouldAdmitThroughRegulatedAssetTemplate() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("REGULATED_ASSET_BUYER_V1");

            StatementProgramView program = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);

            assertThat(program.getStatus()).isEqualTo(StatementProgramView.StatusEnum.IN_PROGRESS);
            assertThat(program.getTemplateName()).isEqualTo("REGULATED_ASSET_BUYER_V1");

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");

            List<String> toAnswer = List.of(
                    "LEGAL_CAPACITY", "BENEFICIAL_OWNER_DISCLOSURE", "SANCTIONS_CLEARANCE",
                    "EXPORT_CONTROL_ELIGIBILITY", "SECTOR_LICENSE",
                    "ENVIRONMENTAL_HANDLING_CAPACITY", "PAYMENT_READINESS", "TERMS_ACCEPTANCE");

            for (String statementCode : toAnswer) {
                SubmitStatementAnswerResponse result = buyerParticipationApi.submitStatementAnswer(
                        auction.getAuctionId(), statementCode, yesAnswer, API_VERSION);
                assertThat(result.getResultType())
                        .isIn(SubmitStatementAnswerResponse.ResultTypeEnum.ACCEPTED,
                                SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);
            }

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);
            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.ADMITTED);
        }

        @Test
        @DisplayName("Should reject candidate and cascade through regulated asset graph")
        void shouldRejectAndCascadeThroughRegulatedGraph() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("REGULATED_ASSET_BUYER_V1");

            buyerParticipationApi.startParticipationProgram(auction.getAuctionId(), startReq, API_VERSION);

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);
            buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "BENEFICIAL_OWNER_DISCLOSURE", yesAnswer, API_VERSION);

            SubmitStatementAnswerRequest noAnswer = new SubmitStatementAnswerRequest();
            noAnswer.setAnswerValue("NO");
            SubmitStatementAnswerResponse rejectResult = buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "SANCTIONS_CLEARANCE", noAnswer, API_VERSION);

            assertThat(rejectResult.getResultType()).isEqualTo(SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);
            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.REJECTED);
            assertThat(decision.getRootCause()).isEqualTo("SANCTIONS_CLEARANCE");
            assertThat(decision.getCascadedStatements()).contains("EXPORT_CONTROL_ELIGIBILITY", "PAYMENT_READINESS");
        }
    }

    @Nested
    @DisplayName("SC7 — High value tender template with complex graph")
    class IT_SC7_HighValueTender {

        @Test
        @DisplayName("Should admit candidate through high value tender template")
        void shouldAdmitThroughHighValueTenderTemplate() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("HIGH_VALUE_TENDER_V1");

            StatementProgramView program = buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION);
            assertThat(program.getTemplateName()).isEqualTo("HIGH_VALUE_TENDER_V1");

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");

            List<String> toAnswer = List.of(
                    "LEGAL_CAPACITY", "BENEFICIAL_OWNER_DISCLOSURE", "SANCTIONS_CLEARANCE",
                    "PEP_DISCLOSURE", "NO_CONFLICT_OF_INTEREST", "NO_COLLUSION",
                    "SOURCE_OF_FUNDS", "BID_BOND_ACCEPTANCE",
                    "DATA_ROOM_CONFIDENTIALITY", "INSIDER_INFORMATION_ABSENCE",
                    "TERMS_ACCEPTANCE");

            for (String statementCode : toAnswer) {
                SubmitStatementAnswerResponse result = buyerParticipationApi.submitStatementAnswer(
                        auction.getAuctionId(), statementCode, yesAnswer, API_VERSION);
                assertThat(result.getResultType())
                        .isIn(SubmitStatementAnswerResponse.ResultTypeEnum.ACCEPTED,
                                SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);
            }

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);
            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.ADMITTED);
        }

        @Test
        @DisplayName("Should reject when conflict of interest is declared in high value tender")
        void shouldRejectOnConflictOfInterest() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("HIGH_VALUE_TENDER_V1");

            buyerParticipationApi.startParticipationProgram(auction.getAuctionId(), startReq, API_VERSION);

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);
            buyerParticipationApi.submitStatementAnswer(auction.getAuctionId(), "BENEFICIAL_OWNER_DISCLOSURE", yesAnswer, API_VERSION);
            buyerParticipationApi.submitStatementAnswer(auction.getAuctionId(), "PEP_DISCLOSURE", yesAnswer, API_VERSION);

            SubmitStatementAnswerRequest noAnswer = new SubmitStatementAnswerRequest();
            noAnswer.setAnswerValue("NO");
            SubmitStatementAnswerResponse rejectResult = buyerParticipationApi.submitStatementAnswer(
                    auction.getAuctionId(), "NO_CONFLICT_OF_INTEREST", noAnswer, API_VERSION);

            assertThat(rejectResult.getResultType()).isEqualTo(SubmitStatementAnswerResponse.ResultTypeEnum.REJECTED);

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);
            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.REJECTED);
            assertThat(decision.getRootCause()).isEqualTo("NO_CONFLICT_OF_INTEREST");
            assertThat(decision.getCascadedStatements()).contains("NO_COLLUSION", "TERMS_ACCEPTANCE");
        }
    }

    @Nested
    @DisplayName("SC8 — Decision is PENDING when statements are missing")
    class IT_SC8_PendingDecision {

        @Test
        @DisplayName("Should return PENDING decision when not all statements are answered")
        void shouldReturnPendingWhenStatementsMissing() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            buyerParticipationApi.startParticipationProgram(auction.getAuctionId(), startReq, API_VERSION);

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);

            ParticipationDecisionView decision = buyerParticipationApi.getParticipationDecision(
                    auction.getAuctionId(), API_VERSION);

            assertThat(decision.getStatus()).isEqualTo(ParticipationDecisionView.StatusEnum.PENDING);
            assertThat(decision.getMissingStatements()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("SC9 — Cannot start program with unknown template")
    class IT_SC9_UnknownTemplate {

        @Test
        @DisplayName("Should return error when starting program with unknown template name")
        void shouldRejectUnknownTemplateName() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("NONEXISTENT_TEMPLATE");

            assertThatThrownBy(() -> buyerParticipationApi.startParticipationProgram(
                    auction.getAuctionId(), startReq, API_VERSION))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Nested
    @DisplayName("SC10 — Program view shows completed statement with answer value")
    class IT_SC10_CompletedStatementDetails {

        @Test
        @DisplayName("Should show answer value in completed statements")
        void shouldShowAnswerValueInCompletedStatements() throws Exception {
            AuctionResponse auction = createActivatedAuction();

            StartParticipationProgramRequest startReq = new StartParticipationProgramRequest();
            startReq.setTemplateName("PUBLIC_CONSUMER_LIGHT_V1");

            buyerParticipationApi.startParticipationProgram(auction.getAuctionId(), startReq, API_VERSION);

            SubmitStatementAnswerRequest yesAnswer = new SubmitStatementAnswerRequest();
            yesAnswer.setAnswerValue("YES");
            buyerParticipationApi.submitStatementAnswer(auction.getAuctionId(), "LEGAL_CAPACITY", yesAnswer, API_VERSION);

            StatementProgramView program = buyerParticipationApi.getParticipationProgram(auction.getAuctionId(), API_VERSION);

            StatementStepView legalCapacity = program.getCompletedStatements().stream()
                    .filter(s -> s.getStatementCode().equals("LEGAL_CAPACITY"))
                    .findFirst()
                    .orElseThrow();

            assertThat(legalCapacity.getAnswerValue()).isEqualTo("YES");
            assertThat(legalCapacity.getTitle()).isNotBlank();
            assertThat(legalCapacity.getQuestion()).isNotBlank();
        }
    }

    private AuctionResponse createActivatedAuction() throws Exception {
        CreateAuctionRequest req = auctionRequest("Participation test auction", "1000.00");
        AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);
        sellerAuctionsApi.activateAuction(auction.getAuctionId(), API_VERSION);
        return auction;
    }
}
