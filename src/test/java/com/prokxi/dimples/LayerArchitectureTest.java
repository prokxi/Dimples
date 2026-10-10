package com.prokxi.dimples;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Enforces the ports & adapters dependency rules from AGENTS.md. If a rule fails, fix the
 * code, not the rule. Changing a rule needs the maintainer's approval.
 */
@AnalyzeClasses(packages = "com.prokxi.dimples", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerArchitectureTest {

	private static final String BASE = "com.prokxi.dimples";

	@ArchTest
	static final ArchRule layerDependencies = layeredArchitecture().consideringOnlyDependenciesInLayers()
		.withOptionalLayers(true)
		.layer("Config")
		.definedBy(BASE + ".config..")
		.layer("Inbound")
		.definedBy(BASE + ".inbound..")
		.layer("Outbound")
		.definedBy(BASE + ".outbound..")
		.layer("Service")
		.definedBy(BASE + ".service..")
		.layer("Domain")
		.definedBy(BASE + ".domain..")
		.layer("Exception")
		.definedBy(BASE + ".exception..")
		.whereLayer("Config")
		.mayNotBeAccessedByAnyLayer()
		.whereLayer("Inbound")
		.mayOnlyBeAccessedByLayers("Config")
		.whereLayer("Outbound")
		.mayOnlyBeAccessedByLayers("Config")
		.whereLayer("Service")
		.mayOnlyBeAccessedByLayers("Inbound", "Outbound", "Config")
		.whereLayer("Domain")
		.mayOnlyBeAccessedByLayers("Service", "Inbound", "Outbound", "Config")
		.whereLayer("Exception")
		.mayOnlyBeAccessedByLayers("Service", "Inbound", "Outbound", "Config")
		.because("dependencies must follow inbound -> service -> domain <- outbound (see AGENTS.md, Architecture)");

	@ArchTest
	static final ArchRule domainIsPureJava = classes().that()
		.resideInAPackage(BASE + ".domain..")
		.should()
		.onlyDependOnClassesThat()
		.resideInAnyPackage("java..", BASE + ".domain..")
		.because("the domain is plain Java and must not depend on Spring, other frameworks or other project packages");

	@ArchTest
	static final ArchRule serviceIsFreeOfAdapterTechnology = noClasses().that()
		.resideInAPackage(BASE + ".service..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("org.springframework.web..", "org.springframework.http..", "jakarta.servlet..",
				"org.springframework.jdbc..", "org.springframework.data.jdbc..",
				"org.springframework.data.relational..", "org.springframework.data.repository..")
		.because("services talk to the outside world only through interfaces they define themselves");

	@ArchTest
	static final ArchRule exceptionDependsOnNoOtherLayer = noClasses().that()
		.resideInAPackage(BASE + ".exception..")
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(BASE + ".inbound..", BASE + ".outbound..", BASE + ".service..", BASE + ".config..")
		.because("shared exception types must not pull in adapters or services");

}
