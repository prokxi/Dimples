package com.prokxi.dimples;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;

/**
 * Enforces the coding conventions from AGENTS.md that can be checked on bytecode.
 */
@AnalyzeClasses(packages = "com.prokxi.dimples", importOptions = ImportOption.DoNotIncludeTests.class)
class CodingRulesArchitectureTest {

	private static final String BASE = "com.prokxi.dimples";

	@ArchTest
	static final ArchRule noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
		.because("use constructor injection (final fields + @RequiredArgsConstructor)");

	@ArchTest
	static final ArchRule noStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
		.because("log with @Slf4j instead of System.out/System.err");

	@ArchTest
	static final ArchRule noJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

	@ArchTest
	static final ArchRule noJodaTime = NO_CLASSES_SHOULD_USE_JODATIME;

	@ArchTest
	static final ArchRule noJpa = noClasses().should()
		.dependOnClassesThat()
		.resideInAnyPackage("jakarta.persistence..", "javax.persistence..", "org.hibernate.annotations..")
		.because("this project uses Spring Data JDBC, not JPA");

	@ArchTest
	static final ArchRule noLegacyDateTypes = noClasses().should()
		.dependOnClassesThat()
		.belongToAnyOf(java.util.Date.class, java.util.Calendar.class, java.sql.Date.class, java.sql.Timestamp.class)
		.because("use java.time (Instant for timestamps, LocalDate for dates)");

	@ArchTest
	static final ArchRule noSystemClockAccess = noClasses().should()
		.callMethod(Instant.class, "now")
		.orShould()
		.callMethod(LocalDate.class, "now")
		.orShould()
		.callMethod(LocalDateTime.class, "now")
		.orShould()
		.callMethod(OffsetDateTime.class, "now")
		.orShould()
		.callMethod(ZonedDateTime.class, "now")
		.orShould()
		.callMethod(System.class, "currentTimeMillis")
		.because("inject a java.time.Clock and use Instant.now(clock) so tests can control time");

	@ArchTest
	static final ArchRule transactionsOnlyInServices = noClasses().that()
		.resideOutsideOfPackage(BASE + ".service..")
		.should()
		.beAnnotatedWith(Transactional.class)
		.orShould()
		.beAnnotatedWith("jakarta.transaction.Transactional")
		.because("@Transactional belongs on service methods that write");

	@ArchTest
	static final ArchRule transactionalMethodsOnlyInServices = noMethods().that()
		.areDeclaredInClassesThat()
		.resideOutsideOfPackage(BASE + ".service..")
		.should()
		.beAnnotatedWith(Transactional.class)
		.because("@Transactional belongs on service methods that write");

	@ArchTest
	static final ArchRule controllersReturnDtos = methods().that()
		.areDeclaredInClassesThat()
		.areMetaAnnotatedWith(Controller.class)
		.and()
		.arePublic()
		.should(notExposeTypesFrom(BASE + ".domain", BASE + ".outbound"))
		.because("controllers return DTO records from inbound, never domain objects or persistence rows");

	private static ArchCondition<JavaMethod> notExposeTypesFrom(String... packages) {
		return new ArchCondition<>("not expose types from " + String.join(", ", packages)) {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				for (JavaClass type : method.getReturnType().getAllInvolvedRawTypes()) {
					for (String pkg : packages) {
						if (type.getPackageName().equals(pkg) || type.getPackageName().startsWith(pkg + ".")) {
							events.add(SimpleConditionEvent.violated(method, method.getFullName() + " returns "
									+ method.getReturnType().getName() + ", which exposes " + type.getName()));
						}
					}
				}
			}
		};
	}

}
