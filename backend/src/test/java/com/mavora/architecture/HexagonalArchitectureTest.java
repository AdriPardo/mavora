package com.mavora.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.mavora", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "org.hibernate..",
                    "org.flywaydb..",
                    "org.springdoc..",
                    "org.openapi4j..",
                    "com.openai..",
                    "org.testcontainers.."
            )
            .because("the domain must stay free of Spring, JPA, HTTP and LLM SDKs");

    @ArchTest
    static final ArchRule domainMustNotDependOnInfrastructureOrApi = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..api..")
            .because("inner domain cannot depend on adapters");

    @ArchTest
    static final ArchRule apiMustNotDependOnJpa = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "org.springframework.data.jpa..",
                    "org.hibernate.."
            )
            .because("controllers must not use JPA entities or repositories directly");

    @ArchTest
    static final ArchRule applicationMustNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("use cases depend on ports, not adapters");

    @ArchTest
    static final ArchRule identityDomainMustNotDependOnOrganization = noClasses()
            .that().resideInAPackage("com.mavora.identity.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.mavora.organization..")
            .because("identity domain stays independent of tenancy internals");
}
