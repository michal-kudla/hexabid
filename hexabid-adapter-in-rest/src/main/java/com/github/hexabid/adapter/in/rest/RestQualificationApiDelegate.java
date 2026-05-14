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
import java.util.Set;

@Service
public class RestQualificationApiDelegate implements QualificationApiDelegate {

    private static final Set<String> REGULATED_CATEGORIES = Set.of("LAND", "ALCOHOL", "VEHICLE");
    private static final Set<String> HIGH_VALUE_CATEGORIES = Set.of("LAND", "ART", "PHARMA", "CONTROLLED_SUBSTANCE");

    @Override
    public ResponseEntity<QualificationProfileListResponse> browseQualificationProfiles(
            String xAPIVersion, String category, String jurisdiction) {
        List<QualificationProfileSummary> all = allProfiles();
        List<QualificationProfileSummary> filtered = filterByCategory(all, category);

        QualificationProfileListResponse response = new QualificationProfileListResponse();
        response.setItems(filtered);
        return ResponseEntity.ok(response);
    }

    private List<QualificationProfileSummary> filterByCategory(
            List<QualificationProfileSummary> profiles, String category) {
        if (category == null || category.isBlank()) {
            return profiles;
        }
        List<QualificationProfileSummary> result = new ArrayList<>();
        for (QualificationProfileSummary p : profiles) {
            if (matchesCategory(p.getTemplateName(), category)) {
                result.add(p);
            }
        }
        if (result.isEmpty()) {
            return profiles;
        }
        return result;
    }

    private boolean matchesCategory(String templateName, String category) {
        if ("PUBLIC_CONSUMER_LIGHT_V1".equals(templateName)) {
            return true;
        }
        if ("REGULATED_ASSET_BUYER_V1".equals(templateName)) {
            return REGULATED_CATEGORIES.contains(category);
        }
        if ("HIGH_VALUE_TENDER_V1".equals(templateName)) {
            return HIGH_VALUE_CATEGORIES.contains(category);
        }
        return true;
    }

    private List<QualificationProfileSummary> allProfiles() {
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
        return items;
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
