package com.github.hexabid.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests for authorization module.
 * <p>
 * Verifies that authorization-core is independent of Spring and JPA,
 * and that it does not depend on hexabid-core or hexabid-auth-core.
 */
class AuthorizationArchitectureTest {

    private static final String AUTH_CORE = "com.github.hexabid.authorization.core";

    @Test
    void authorizationCoreMustNotDependOnSpring() {
        var importedClasses = new ClassFileImporter().importPackages(AUTH_CORE);

        noClasses().that().resideInAPackage(AUTH_CORE + "..")
                .should().accessClassesThat().resideInAnyPackage("..org.springframework..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void authorizationCoreMustNotDependOnJpa() {
        var importedClasses = new ClassFileImporter().importPackages(AUTH_CORE);

        noClasses().that().resideInAPackage(AUTH_CORE + "..")
                .should().accessClassesThat().resideInAnyPackage("..jakarta.persistence..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void authorizationCoreMustNotDependOnHexabidCore() {
        var importedClasses = new ClassFileImporter().importPackages(AUTH_CORE);

        noClasses().that().resideInAPackage(AUTH_CORE + "..")
                .should().accessClassesThat().resideInAnyPackage("..com.github.hexabid.core..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void authorizationCoreMustNotDependOnAuthCore() {
        var importedClasses = new ClassFileImporter().importPackages(AUTH_CORE);

        noClasses().that().resideInAPackage(AUTH_CORE + "..")
                .should().accessClassesThat().resideInAnyPackage("..com.github.hexabid.auth.core..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }
}
