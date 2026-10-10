package com.prokxi.dimples;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the naming conventions from AGENTS.md: every kind of class has a fixed package
 * and name pattern.
 */
@AnalyzeClasses(packages = "com.prokxi.dimples", importOptions = ImportOption.DoNotIncludeTests.class)
class NamingArchitectureTest {

	private static final String BASE = "com.prokxi.dimples";

	@ArchTest
	static final ArchRule controllersLiveInInbound = classes().that()
		.areMetaAnnotatedWith(Controller.class)
		.should()
		.resideInAPackage(BASE + ".inbound..")
		.andShould()
		.haveSimpleNameEndingWith("Controller");

	@ArchTest
	static final ArchRule controllerNamesAreReservedForControllers = classes().that()
		.haveSimpleNameEndingWith("Controller")
		.should()
		.beMetaAnnotatedWith(Controller.class)
		.andShould()
		.resideInAPackage(BASE + ".inbound..");

	@ArchTest
	static final ArchRule requestAndResponseDtosAreRecordsInInbound = classes().that()
		.haveSimpleNameEndingWith("Request")
		.or()
		.haveSimpleNameEndingWith("Response")
		.should()
		.resideInAPackage(BASE + ".inbound..")
		.andShould()
		.beAssignableTo(Record.class)
		.because("request/response DTOs are records in inbound");

	@ArchTest
	static final ArchRule springDataRepositoriesLiveInOutbound = classes().that()
		.areAssignableTo(Repository.class)
		.should()
		.resideInAPackage(BASE + ".outbound..")
		.andShould()
		.haveSimpleNameEndingWith("JdbcRepository")
		.andShould()
		.onlyHaveDependentClassesThat()
		.haveSimpleNameEndingWith("Impl")
		.because("Spring Data repositories are internal to outbound and used only by the matching ...Impl");

	@ArchTest
	static final ArchRule implementationsLiveInOutbound = classes().that()
		.haveSimpleNameEndingWith("Impl")
		.should()
		.resideInAPackage(BASE + ".outbound..");

	@ArchTest
	static final ArchRule persistenceRowsLiveInOutbound = classes().that()
		.haveSimpleNameEndingWith("Row")
		.should()
		.resideInAPackage(BASE + ".outbound..");

	@ArchTest
	static final ArchRule configurationLivesInConfig = classes().that()
		.areMetaAnnotatedWith(Configuration.class)
		.and(not(equivalentTo(DimplesApplication.class)))
		.should()
		.resideInAPackage(BASE + ".config..")
		.andShould()
		.haveSimpleNameEndingWith("Config");

	@ArchTest
	static final ArchRule controllerAdviceLivesInException = classes().that()
		.areMetaAnnotatedWith(ControllerAdvice.class)
		.should()
		.resideInAPackage(BASE + ".exception..");

	@ArchTest
	static final ArchRule exceptionsLiveInException = classes().that()
		.areAssignableTo(Throwable.class)
		.should()
		.resideInAPackage(BASE + ".exception..")
		.andShould()
		.haveSimpleNameEndingWith("Exception")
		.because("shared exception types live in exception; the domain signals broken invariants with JDK exceptions");

	@ArchTest
	static final ArchRule noPortOrAdapterSuffixes = noClasses().should()
		.haveNameMatching(".*(Port|Adapter)")
		.because("names are plain: RoundRepository / RoundRepositoryImpl, not RoundRepositoryPort / ...Adapter");

}
