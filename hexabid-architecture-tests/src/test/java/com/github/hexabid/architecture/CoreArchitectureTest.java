package com.github.hexabid.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CoreArchitectureTest {

    @Test
    void coreMustNotDependOnFrameworksOrPersistenceAnnotations() {
        var importedClasses = new ClassFileImporter().importPackages("com.github.hexabid.core");

        noClasses().that().resideInAPackage("..core..")
                .should().accessClassesThat().resideInAnyPackage(
                        "..org.springframework..",
                        "..jakarta.persistence.."
                )
                .check(importedClasses);
    }
}
