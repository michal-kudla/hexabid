package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.contract.api.QualificationApiDelegate;
import com.github.hexabid.contract.model.QualificationProfileListResponse;
import com.github.hexabid.contract.model.QualificationProfileSummary;
import com.github.hexabid.statement.template.ParticipationPolicyTemplate;
import com.github.hexabid.statement.template.PolicyTemplateCatalog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestQualificationApiDelegate implements QualificationApiDelegate {

    @Override
    public ResponseEntity<QualificationProfileListResponse> browseQualificationProfiles(String xAPIVersion) {
        List<QualificationProfileSummary> items = new ArrayList<>();
        items.add(toSummary(PolicyTemplateCatalog.PUBLIC_CONSUMER_LIGHT_V1, "Standardowy konsument",
                "Podstawowy pakiet dla zwykłych aukcji konsumenckich. Wymaga oświadczeń o tożsamości i akceptacji regulaminu.",
                4, "2-3", QualificationProfileSummary.AbandonmentRiskEnum.LOW, true));
        items.add(toSummary(PolicyTemplateCatalog.REGULATED_ASSET_BUYER_V1, "Nabywca regulowany",
                "Pakiet dla aukcji z wymogami regulacyjnymi: grunt, nieruchomości, akcyza. Wymaga dodatkowych oświadczeń o rezydencji i uprawnieniach.",
                8, "5-8", QualificationProfileSummary.AbandonmentRiskEnum.MEDIUM, false));
        items.add(toSummary(PolicyTemplateCatalog.HIGH_VALUE_TENDER_V1, "Przetarg wysokiej wartości",
                "Pakiet dla aukcji o wysokiej wartości: AML, sankcje, beneficjent rzeczywisty. Zalecany dla transakcji powyżej 10 000 EUR.",
                11, "8-12", QualificationProfileSummary.AbandonmentRiskEnum.HIGH, false));

        QualificationProfileListResponse response = new QualificationProfileListResponse();
        response.setItems(items);
        return ResponseEntity.ok(response);
    }

    private QualificationProfileSummary toSummary(
            ParticipationPolicyTemplate template,
            String label,
            String description,
            int taskCount,
            String estimatedMinutes,
            QualificationProfileSummary.AbandonmentRiskEnum abandonmentRisk,
            boolean recommended
    ) {
        QualificationProfileSummary summary = new QualificationProfileSummary();
        summary.setTemplateName(template.name());
        summary.setLabel(label);
        summary.setDescription(description);
        summary.setTaskCount(taskCount);
        summary.setEstimatedMinutes(estimatedMinutes);
        summary.setAbandonmentRisk(abandonmentRisk);
        summary.setRecommended(recommended);
        return summary;
    }
}
