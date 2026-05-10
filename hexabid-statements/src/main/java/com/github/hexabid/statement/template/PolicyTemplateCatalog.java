package com.github.hexabid.statement.template;

import com.github.hexabid.statement.graph.StatementDependencyGraph;
import com.github.hexabid.statement.model.AnswerType;
import com.github.hexabid.statement.model.DisqualifyingAnswer;
import com.github.hexabid.statement.model.ParticipationPolicyTemplateId;
import com.github.hexabid.statement.model.PolicyTemplateVersion;
import com.github.hexabid.statement.model.StatementCategory;
import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementDefinition;
import com.github.hexabid.statement.model.StatementDependency;
import com.github.hexabid.statement.model.StatementSeverity;
import com.github.hexabid.statement.model.StatementStep;
import com.github.hexabid.statement.model.StatementViolationDefinition;
import com.github.hexabid.statement.model.StatementViolationType;

import java.util.List;
import java.util.UUID;

/**
 * Static catalog of well-known {@link ParticipationPolicyTemplate} instances.
 *
 * <p>Each constant defines a pre-configured template for a specific auction
 * participation scenario, with all statements, dependencies, and steps fully
 * specified. Templates are identified by stable UUIDs to ensure cross-service
 * referencing remains consistent.
 *
 * <p>Available templates:
 * <ul>
 *   <li>{@link #PUBLIC_CONSUMER_LIGHT_V1} — lightweight qualification for public consumer auctions</li>
 *   <li>{@link #REGULATED_ASSET_BUYER_V1} — enhanced qualification for regulated asset purchases</li>
 *   <li>{@link #HIGH_VALUE_TENDER_V1} — full qualification for high-value tender auctions</li>
 * </ul>
 */
public final class PolicyTemplateCatalog {

    private PolicyTemplateCatalog() {}

    /** Lightweight participation policy for public consumer auctions. */
    public static final ParticipationPolicyTemplate PUBLIC_CONSUMER_LIGHT_V1 = buildPublicConsumerLight();

    /** Enhanced participation policy for regulated asset purchases requiring sector licenses. */
    public static final ParticipationPolicyTemplate REGULATED_ASSET_BUYER_V1 = buildRegulatedAssetBuyer();

    /** Comprehensive participation policy for high-value tenders with anti-collusion and AML checks. */
    public static final ParticipationPolicyTemplate HIGH_VALUE_TENDER_V1 = buildHighValueTender();

    private static ParticipationPolicyTemplate buildPublicConsumerLight() {
        List<StatementDefinition> statements = List.of(
                new StatementDefinition(
                        StatementCode.LEGAL_CAPACITY, StatementCategory.IDENTITY,
                        "Zdolność do czynności prawnych",
                        "Czy masz zdolność do czynności prawnych lub działasz przez poprawnego pełnomocnika?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak zdolności do czynności prawnych")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.LEGAL_CAPACITY, "Kandydat nie ma zdolności do czynności prawnych"))
                ),
                new StatementDefinition(
                        StatementCode.SANCTIONS_CLEARANCE, StatementCategory.COMPLIANCE,
                        "Brak na listach sankcyjnych",
                        "Czy jesteś wolny od wpisów na listy sankcyjne?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Osoba na liście sankcyjnej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.SANCTIONS_CLEARANCE, "Kandydat znajduje się na liście sankcyjnej"))
                ),
                new StatementDefinition(
                        StatementCode.PAYMENT_READINESS, StatementCategory.FINANCING,
                        "Gotowość płatnicza",
                        "Czy potwierdzasz możliwość zapłaty w terminie?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak gotowości płatniczej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.PAYMENT_READINESS, "Kandydat nie potwierdził gotowości płatniczej"))
                ),
                new StatementDefinition(
                        StatementCode.TERMS_ACCEPTANCE, StatementCategory.COMMITMENT,
                        "Akceptacja regulaminu",
                        "Czy akceptujesz regulamin aukcji, opłaty, terminy i konsekwencje?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Odmowa akceptacji regulaminu")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.TERMS_ACCEPTANCE, "Kandydat nie zaakceptował regulaminu"))
                )
        );

        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addNode(StatementCode.LEGAL_CAPACITY)
                .addNode(StatementCode.SANCTIONS_CLEARANCE)
                .addNode(StatementCode.PAYMENT_READINESS)
                .addNode(StatementCode.TERMS_ACCEPTANCE)
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.PAYMENT_READINESS))
                .addEdge(StatementDependency.requires(StatementCode.PAYMENT_READINESS, StatementCode.TERMS_ACCEPTANCE))
                .build();

        List<StatementStep> steps = List.of(
                new StatementStep(StatementCode.LEGAL_CAPACITY, 1, "Profil i umocowanie"),
                new StatementStep(StatementCode.SANCTIONS_CLEARANCE, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.PAYMENT_READINESS, 3, "Zdolność wykonania i finansowanie"),
                new StatementStep(StatementCode.TERMS_ACCEPTANCE, 4, "Zobowiązanie końcowe")
        );

        return new ParticipationPolicyTemplate(
                ParticipationPolicyTemplateId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                "PUBLIC_CONSUMER_LIGHT_V1",
                PolicyTemplateVersion.v1(),
                statements,
                graph,
                steps
        );
    }

    private static ParticipationPolicyTemplate buildRegulatedAssetBuyer() {
        List<StatementDefinition> statements = List.of(
                new StatementDefinition(
                        StatementCode.LEGAL_CAPACITY, StatementCategory.IDENTITY,
                        "Zdolność do czynności prawnych",
                        "Czy masz zdolność do czynności prawnych lub działasz przez poprawnego pełnomocnika?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak zdolności do czynności prawnych")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.LEGAL_CAPACITY, "Kandydat nie ma zdolności do czynności prawnych"))
                ),
                new StatementDefinition(
                        StatementCode.BENEFICIAL_OWNER_DISCLOSURE, StatementCategory.IDENTITY,
                        "Ujawnienie beneficjenta rzeczywistego",
                        "Czy ujawniono beneficjenta rzeczywistego i strukturę właścicielską?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak ujawnienia beneficjenta")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.BENEFICIAL_OWNER_DISCLOSURE, "Nie ujawniono beneficjenta rzeczywistego"))
                ),
                new StatementDefinition(
                        StatementCode.SANCTIONS_CLEARANCE, StatementCategory.COMPLIANCE,
                        "Brak na listach sankcyjnych",
                        "Czy jesteś wolny od wpisów na listy sankcyjne?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Osoba na liście sankcyjnej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.SANCTIONS_CLEARANCE, "Kandydat znajduje się na liście sankcyjnej"))
                ),
                new StatementDefinition(
                        StatementCode.EXPORT_CONTROL_ELIGIBILITY, StatementCategory.REGULATORY,
                        "Uprawnienie do kontroli eksportu",
                        "Czy możesz nabyć towar objęty kontrolą eksportu?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak uprawnienia do kontroli eksportu")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.EXPORT_CONTROL_ELIGIBILITY, "Kandydat nie ma uprawnienia eksportowego"))
                ),
                new StatementDefinition(
                        StatementCode.SECTOR_LICENSE, StatementCategory.REGULATORY,
                        "Licencja branżowa",
                        "Czy posiadasz licencję branżową wymaganą do nabycia tego przedmiotu?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak licencji branżowej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.SECTOR_LICENSE, "Kandydat nie posiada wymaganej licencji"))
                ),
                new StatementDefinition(
                        StatementCode.ENVIRONMENTAL_HANDLING_CAPACITY, StatementCategory.EXECUTION,
                        "Zdolność do postępowania z przedmiotem",
                        "Czy potrafisz legalnie odebrać, przewieźć lub zutylizować przedmiot?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak zdolności środowiskowej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.ENVIRONMENTAL_HANDLING_CAPACITY, "Kandydat nie ma zdolności środowiskowej"))
                ),
                new StatementDefinition(
                        StatementCode.PAYMENT_READINESS, StatementCategory.FINANCING,
                        "Gotowość płatnicza",
                        "Czy potwierdzasz możliwość zapłaty w terminie?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak gotowości płatniczej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.PAYMENT_READINESS, "Kandydat nie potwierdził gotowości płatniczej"))
                ),
                new StatementDefinition(
                        StatementCode.TERMS_ACCEPTANCE, StatementCategory.COMMITMENT,
                        "Akceptacja regulaminu",
                        "Czy akceptujesz regulamin aukcji?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Odmowa akceptacji regulaminu")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.TERMS_ACCEPTANCE, "Kandydat nie zaakceptował regulaminu"))
                )
        );

        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addNode(StatementCode.LEGAL_CAPACITY)
                .addNode(StatementCode.BENEFICIAL_OWNER_DISCLOSURE)
                .addNode(StatementCode.SANCTIONS_CLEARANCE)
                .addNode(StatementCode.EXPORT_CONTROL_ELIGIBILITY)
                .addNode(StatementCode.SECTOR_LICENSE)
                .addNode(StatementCode.ENVIRONMENTAL_HANDLING_CAPACITY)
                .addNode(StatementCode.PAYMENT_READINESS)
                .addNode(StatementCode.TERMS_ACCEPTANCE)
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.BENEFICIAL_OWNER_DISCLOSURE))
                .addEdge(StatementDependency.requires(StatementCode.BENEFICIAL_OWNER_DISCLOSURE, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.EXPORT_CONTROL_ELIGIBILITY))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.PAYMENT_READINESS))
                .addEdge(StatementDependency.requires(StatementCode.SECTOR_LICENSE, StatementCode.ENVIRONMENTAL_HANDLING_CAPACITY))
                .addEdge(StatementDependency.requires(StatementCode.PAYMENT_READINESS, StatementCode.TERMS_ACCEPTANCE))
                .build();

        List<StatementStep> steps = List.of(
                new StatementStep(StatementCode.LEGAL_CAPACITY, 1, "Profil i umocowanie"),
                new StatementStep(StatementCode.BENEFICIAL_OWNER_DISCLOSURE, 1, "Profil i umocowanie"),
                new StatementStep(StatementCode.SANCTIONS_CLEARANCE, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.EXPORT_CONTROL_ELIGIBILITY, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.SECTOR_LICENSE, 3, "Zdolność wykonania"),
                new StatementStep(StatementCode.ENVIRONMENTAL_HANDLING_CAPACITY, 3, "Zdolność wykonania"),
                new StatementStep(StatementCode.PAYMENT_READINESS, 3, "Finansowanie"),
                new StatementStep(StatementCode.TERMS_ACCEPTANCE, 4, "Zobowiązanie końcowe")
        );

        return new ParticipationPolicyTemplate(
                ParticipationPolicyTemplateId.of(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                "REGULATED_ASSET_BUYER_V1",
                PolicyTemplateVersion.v1(),
                statements,
                graph,
                steps
        );
    }

    private static ParticipationPolicyTemplate buildHighValueTender() {
        List<StatementDefinition> statements = List.of(
                new StatementDefinition(
                        StatementCode.LEGAL_CAPACITY, StatementCategory.IDENTITY,
                        "Zdolność do czynności prawnych",
                        "Czy masz zdolność do czynności prawnych?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak zdolności do czynności prawnych")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.LEGAL_CAPACITY, "Kandydat nie ma zdolności do czynności prawnych"))
                ),
                new StatementDefinition(
                        StatementCode.BENEFICIAL_OWNER_DISCLOSURE, StatementCategory.IDENTITY,
                        "Ujawnienie beneficjenta rzeczywistego",
                        "Czy ujawniono beneficjenta rzeczywistego?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak ujawnienia beneficjenta")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.BENEFICIAL_OWNER_DISCLOSURE, "Nie ujawniono beneficjenta rzeczywistego"))
                ),
                new StatementDefinition(
                        StatementCode.PEP_DISCLOSURE, StatementCategory.COMPLIANCE,
                        "Ujawnienie statusu PEP",
                        "Czy jesteś osobą politycznie eksponowaną?",
                        AnswerType.YES_NO, StatementSeverity.IMPORTANT,
                        List.of(),
                        List.of(new StatementViolationDefinition(StatementViolationType.CONTRADICTORY_DECLARATION,
                                StatementCode.PEP_DISCLOSURE, "Niejawny PEP"))
                ),
                new StatementDefinition(
                        StatementCode.SANCTIONS_CLEARANCE, StatementCategory.COMPLIANCE,
                        "Brak na listach sankcyjnych",
                        "Czy jesteś wolny od wpisów na listy sankcyjne?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Osoba na liście sankcyjnej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.SANCTIONS_CLEARANCE, "Kandydat znajduje się na liście sankcyjnej"))
                ),
                new StatementDefinition(
                        StatementCode.NO_CONFLICT_OF_INTEREST, StatementCategory.COMPLIANCE,
                        "Brak konfliktu interesów",
                        "Czy nie masz relacji z organizatorem, rzeczoznawcą lub sprzedającym?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Konflikt interesów")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.NO_CONFLICT_OF_INTEREST, "Kandydat ma konflikt interesów"))
                ),
                new StatementDefinition(
                        StatementCode.NO_COLLUSION, StatementCategory.FAIRNESS,
                        "Brak porozumienia",
                        "Czy nie uzgadniałeś ofert z innymi kandydatami?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Ryzyko porozumienia")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.NO_COLLUSION, "Ryzyko uzgadniania ofert"))
                ),
                new StatementDefinition(
                        StatementCode.SOURCE_OF_FUNDS, StatementCategory.FINANCING,
                        "Źródło środków",
                        "Czy wskazałeś legalne źródło środków na zakup?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Niezweryfikowane środki")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.SOURCE_OF_FUNDS, "Kandydat nie wskazał źródła środków"))
                ),
                new StatementDefinition(
                        StatementCode.BID_BOND_ACCEPTANCE, StatementCategory.FINANCING,
                        "Akceptacja wadium",
                        "Czy akceptujesz wadium, blokadę środków lub gwarancję?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Brak wadium")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.BID_BOND_ACCEPTANCE, "Kandydat nie zaakceptował wadium"))
                ),
                new StatementDefinition(
                        StatementCode.DATA_ROOM_CONFIDENTIALITY, StatementCategory.CONFIDENTIALITY,
                        "Poufność data room",
                        "Czy przyjmujesz poufność danych z data room?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Odmowa poufności")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.DATA_ROOM_CONFIDENTIALITY, "Kandydat odmówił poufności"))
                ),
                new StatementDefinition(
                        StatementCode.INSIDER_INFORMATION_ABSENCE, StatementCategory.FAIRNESS,
                        "Brak informacji niejawnnej",
                        "Czy nie posiadasz niejawnych informacji dających przewagę?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Ryzyko informacji niejawnej")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.INSIDER_INFORMATION_ABSENCE, "Kandydat posiada niejawne informacje"))
                ),
                new StatementDefinition(
                        StatementCode.TERMS_ACCEPTANCE, StatementCategory.COMMITMENT,
                        "Akceptacja regulaminu",
                        "Czy akceptujesz regulamin aukcji?",
                        AnswerType.YES_NO, StatementSeverity.BLOCKING,
                        List.of(DisqualifyingAnswer.of("NO", "Odmowa akceptacji regulaminu")),
                        List.of(new StatementViolationDefinition(StatementViolationType.FATAL_DECLARATION,
                                StatementCode.TERMS_ACCEPTANCE, "Kandydat nie zaakceptował regulaminu"))
                )
        );

        StatementDependencyGraph graph = StatementDependencyGraph.builder()
                .addNode(StatementCode.LEGAL_CAPACITY)
                .addNode(StatementCode.BENEFICIAL_OWNER_DISCLOSURE)
                .addNode(StatementCode.PEP_DISCLOSURE)
                .addNode(StatementCode.SANCTIONS_CLEARANCE)
                .addNode(StatementCode.NO_CONFLICT_OF_INTEREST)
                .addNode(StatementCode.NO_COLLUSION)
                .addNode(StatementCode.SOURCE_OF_FUNDS)
                .addNode(StatementCode.BID_BOND_ACCEPTANCE)
                .addNode(StatementCode.DATA_ROOM_CONFIDENTIALITY)
                .addNode(StatementCode.INSIDER_INFORMATION_ABSENCE)
                .addNode(StatementCode.TERMS_ACCEPTANCE)
                .addEdge(StatementDependency.requires(StatementCode.LEGAL_CAPACITY, StatementCode.BENEFICIAL_OWNER_DISCLOSURE))
                .addEdge(StatementDependency.requires(StatementCode.BENEFICIAL_OWNER_DISCLOSURE, StatementCode.SANCTIONS_CLEARANCE))
                .addEdge(StatementDependency.requires(StatementCode.BENEFICIAL_OWNER_DISCLOSURE, StatementCode.PEP_DISCLOSURE))
                .addEdge(StatementDependency.requires(StatementCode.PEP_DISCLOSURE, StatementCode.NO_CONFLICT_OF_INTEREST))
                .addEdge(StatementDependency.requires(StatementCode.NO_CONFLICT_OF_INTEREST, StatementCode.NO_COLLUSION))
                .addEdge(StatementDependency.requires(StatementCode.NO_CONFLICT_OF_INTEREST, StatementCode.TERMS_ACCEPTANCE))
                .addEdge(StatementDependency.requires(StatementCode.SANCTIONS_CLEARANCE, StatementCode.SOURCE_OF_FUNDS))
                .addEdge(StatementDependency.requires(StatementCode.SOURCE_OF_FUNDS, StatementCode.BID_BOND_ACCEPTANCE))
                .addEdge(StatementDependency.requires(StatementCode.BID_BOND_ACCEPTANCE, StatementCode.TERMS_ACCEPTANCE))
                .addEdge(StatementDependency.requires(StatementCode.DATA_ROOM_CONFIDENTIALITY, StatementCode.INSIDER_INFORMATION_ABSENCE))
                .build();

        List<StatementStep> steps = List.of(
                new StatementStep(StatementCode.LEGAL_CAPACITY, 1, "Profil i umocowanie"),
                new StatementStep(StatementCode.BENEFICIAL_OWNER_DISCLOSURE, 1, "Profil i umocowanie"),
                new StatementStep(StatementCode.SANCTIONS_CLEARANCE, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.PEP_DISCLOSURE, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.NO_CONFLICT_OF_INTEREST, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.NO_COLLUSION, 2, "Wykluczenia twarde"),
                new StatementStep(StatementCode.SOURCE_OF_FUNDS, 3, "Finansowanie"),
                new StatementStep(StatementCode.BID_BOND_ACCEPTANCE, 3, "Finansowanie"),
                new StatementStep(StatementCode.DATA_ROOM_CONFIDENTIALITY, 4, "Zobowiązanie"),
                new StatementStep(StatementCode.INSIDER_INFORMATION_ABSENCE, 4, "Zobowiązanie"),
                new StatementStep(StatementCode.TERMS_ACCEPTANCE, 4, "Zobowiązanie końcowe")
        );

        return new ParticipationPolicyTemplate(
                ParticipationPolicyTemplateId.of(UUID.fromString("00000000-0000-0000-0000-000000000003")),
                "HIGH_VALUE_TENDER_V1",
                PolicyTemplateVersion.v1(),
                statements,
                graph,
                steps
        );
    }
}
