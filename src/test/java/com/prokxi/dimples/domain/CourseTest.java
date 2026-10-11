package com.prokxi.dimples.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.prokxi.dimples.domain.CourseFixtures.tee18;
import static com.prokxi.dimples.domain.CourseFixtures.tee9;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CourseTest {

	private static final CourseId ID = CourseId.generate();

	private static final GolferId CREATOR = GolferId.generate();

	private static final Instant CREATED = Instant.parse("2026-05-01T08:00:00Z");

	private static final Instant NOW = Instant.parse("2026-06-15T12:00:00Z");

	private static final Tee YELLOW = tee18("Yellow");

	private static final Tee WHITE = tee18("White");

	private static Course course(String name, String city, String country) {
		return Course.create(ID, name, city, country, HoleCount.EIGHTEEN, List.of(YELLOW), CREATOR, CREATED);
	}

	private static Course course(List<Tee> tees) {
		return Course.create(ID, "Golfclub Sonnenhof", null, null, HoleCount.EIGHTEEN, tees, CREATOR, CREATED);
	}

	@Nested
	class Creation {

		@Test
		void createsANewCourse() {
			Course course = Course.create(ID, "Golfclub Sonnenhof", "Munich", "DE", HoleCount.EIGHTEEN,
					List.of(YELLOW, WHITE), CREATOR, CREATED);

			assertThat(course.id()).isEqualTo(ID);
			assertThat(course.name()).isEqualTo("Golfclub Sonnenhof");
			assertThat(course.city()).contains("Munich");
			assertThat(course.country()).contains("DE");
			assertThat(course.holeCount()).isEqualTo(HoleCount.EIGHTEEN);
			assertThat(course.tees()).containsExactly(YELLOW, WHITE);
			assertThat(course.createdBy()).isEqualTo(CREATOR);
			assertThat(course.createdAt()).isEqualTo(CREATED);
			assertThat(course.updatedAt()).isEqualTo(CREATED);
			assertThat(course.version()).isZero();
		}

		@Test
		void restoresAStoredCourse() {
			Course course = new Course(ID, "Golfclub Sonnenhof", null, null, HoleCount.NINE, List.of(tee9("Red")),
					CREATOR, CREATED, NOW, 3);

			assertThat(course.holeCount()).isEqualTo(HoleCount.NINE);
			assertThat(course.updatedAt()).isEqualTo(NOW);
			assertThat(course.version()).isEqualTo(3);
		}

		@Test
		void keepsAnUnmodifiableCopyOfTheTees() {
			List<Tee> tees = new ArrayList<>(List.of(YELLOW));
			Course course = course(tees);
			tees.clear();

			assertThat(course.tees()).containsExactly(YELLOW).isUnmodifiable();
		}

		@Test
		void rejectsAnUpdateTimeBeforeTheCreationTime() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Course(ID, "Golfclub Sonnenhof", null, null, HoleCount.EIGHTEEN, List.of(YELLOW),
						CREATOR, NOW, CREATED, 1))
				.withMessage("A course cannot be updated before it was created");
		}

		@Test
		void rejectsMissingValues() {
			List<Tee> tees = List.of(YELLOW);
			HoleCount n = HoleCount.EIGHTEEN;
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(null, "Course", null, null, n, tees, CREATOR, CREATED, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, null, null, null, n, tees, CREATOR, CREATED, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, "Course", null, null, null, tees, CREATOR, CREATED, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, "Course", null, null, n, null, CREATOR, CREATED, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, "Course", null, null, n, tees, null, CREATED, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, "Course", null, null, n, tees, CREATOR, null, NOW, 0));
			assertThatNullPointerException()
				.isThrownBy(() -> new Course(ID, "Course", null, null, n, tees, CREATOR, CREATED, null, 0));
		}

	}

	@Nested
	class Name {

		@Test
		void stripsTheName() {
			assertThat(course("  Golfclub Sonnenhof ", null, null).name()).isEqualTo("Golfclub Sonnenhof");
		}

		@ParameterizedTest
		@ValueSource(ints = { 2, 100 })
		void acceptsNamesWith2To100Characters(int length) {
			String name = "a".repeat(length);

			assertThat(course(name, null, null).name()).isEqualTo(name);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "   ", "a", " a " })
		void rejectsNamesWithFewerThan2Characters(String name) {
			assertThatIllegalArgumentException().isThrownBy(() -> course(name, null, null))
				.withMessage("Course name must have 2 to 100 characters");
		}

		@Test
		void rejectsNamesWithMoreThan100Characters() {
			assertThatIllegalArgumentException().isThrownBy(() -> course("a".repeat(101), null, null))
				.withMessage("Course name must have 2 to 100 characters");
		}

		@Test
		void rejectsControlCharacters() {
			assertThatIllegalArgumentException().isThrownBy(() -> course("Golf\nclub", null, null))
				.withMessage("Course name must not contain control characters");
		}

	}

	@Nested
	class City {

		@ParameterizedTest
		@NullSource
		@ValueSource(strings = { "", "   " })
		void isOptional(String city) {
			assertThat(course("Golfclub Sonnenhof", city, null).city()).isEmpty();
		}

		@Test
		void stripsTheCity() {
			assertThat(course("Golfclub Sonnenhof", " Munich  ", null).city()).contains("Munich");
		}

		@Test
		void acceptsUpTo100Characters() {
			String city = "c".repeat(100);

			assertThat(course("Golfclub Sonnenhof", city, null).city()).contains(city);
		}

		@Test
		void rejectsMoreThan100Characters() {
			assertThatIllegalArgumentException().isThrownBy(() -> course("Golfclub Sonnenhof", "c".repeat(101), null))
				.withMessage("City must have at most 100 characters");
		}

		@Test
		void rejectsControlCharacters() {
			assertThatIllegalArgumentException().isThrownBy(() -> course("Golfclub Sonnenhof", "Mun\u0000ich", null))
				.withMessage("City must not contain control characters");
		}

	}

	@Nested
	class Country {

		@Test
		void isOptional() {
			assertThat(course("Golfclub Sonnenhof", null, null).country()).isEmpty();
		}

		@ParameterizedTest
		@ValueSource(strings = { "DE", "de", "De" })
		void acceptsIsoAlpha2CodesInAnyCase(String country) {
			assertThat(course("Golfclub Sonnenhof", null, country).country()).contains("DE");
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "XX", "DEU", "D", " DE", "Germany" })
		void rejectsAnythingElse(String country) {
			assertThatIllegalArgumentException().isThrownBy(() -> course("Golfclub Sonnenhof", null, country))
				.withMessage("Country must be an ISO 3166-1 alpha-2 code, e.g. DE");
		}

	}

	@Nested
	class Tees {

		@Test
		void requiresAtLeastOneTee() {
			assertThatIllegalArgumentException().isThrownBy(() -> course(List.of()))
				.withMessage("A course needs at least one tee");
		}

		@Test
		void requiresEveryTeeToHaveTheCoursesNumberOfHoles() {
			assertThatIllegalArgumentException().isThrownBy(() -> course(List.of(YELLOW, tee9("Red"))))
				.withMessage("Every tee must have 18 holes");
			assertThatIllegalArgumentException()
				.isThrownBy(() -> Course.create(ID, "Golfclub Sonnenhof", null, null, HoleCount.NINE, List.of(YELLOW),
						CREATOR, CREATED))
				.withMessage("Every tee must have 9 holes");
		}

		@Test
		void requiresUniqueTeeNamesIgnoringCase() {
			assertThatIllegalArgumentException().isThrownBy(() -> course(List.of(YELLOW, tee18("YELLOW"))))
				.withMessage("Tee names must be unique within a course");
		}

		@Test
		void requiresUniqueTeeIds() {
			Tee sameId = new Tee(YELLOW.id(), "White", YELLOW.courseRating(), YELLOW.slopeRating(), null, null,
					YELLOW.holes());

			assertThatIllegalArgumentException().isThrownBy(() -> course(List.of(YELLOW, sameId)))
				.withMessage("Tee IDs must be unique within a course");
		}

		@Test
		void findsATeeById() {
			Course course = course(List.of(YELLOW, WHITE));

			assertThat(course.findTee(WHITE.id())).containsSame(WHITE);
			assertThat(course.findTee(YELLOW.id())).containsSame(YELLOW);
			assertThat(course.findTee(TeeId.generate())).isEmpty();
		}

	}

	@Nested
	class Update {

		@Test
		void replacesTheWholeCourseAndKeepsIdentityAndCreation() {
			Course stored = new Course(ID, "Golfclub Sonnenhof", "Munich", "DE", HoleCount.EIGHTEEN, List.of(YELLOW),
					CREATOR, CREATED, CREATED, 2);
			Tee red = tee9("Red");

			Course updated = stored.update("GC Sonnenhof", null, "AT", HoleCount.NINE, List.of(red), NOW);

			assertThat(updated.id()).isEqualTo(ID);
			assertThat(updated.name()).isEqualTo("GC Sonnenhof");
			assertThat(updated.city()).isEmpty();
			assertThat(updated.country()).contains("AT");
			assertThat(updated.holeCount()).isEqualTo(HoleCount.NINE);
			assertThat(updated.tees()).containsExactly(red);
			assertThat(updated.createdBy()).isEqualTo(CREATOR);
			assertThat(updated.createdAt()).isEqualTo(CREATED);
			assertThat(updated.updatedAt()).isEqualTo(NOW);
			assertThat(updated.version()).isEqualTo(2);
			assertThat(stored.name()).isEqualTo("Golfclub Sonnenhof");
		}

		@Test
		void checksTheSameRulesAsCreation() {
			Course course = course(List.of(YELLOW));

			assertThatIllegalArgumentException()
				.isThrownBy(() -> course.update("Golfclub Sonnenhof", null, null, HoleCount.EIGHTEEN, List.of(), NOW))
				.withMessage("A course needs at least one tee");
		}

		@Test
		void requiresTheTime() {
			Course course = course(List.of(YELLOW));

			assertThatNullPointerException().isThrownBy(
					() -> course.update("Golfclub Sonnenhof", null, null, HoleCount.EIGHTEEN, List.of(YELLOW), null))
				.withMessage("now must not be null");
		}

	}

	@Test
	void coursesWithTheSameIdAreEqual() {
		Course course = course(List.of(YELLOW));
		Course renamed = course("Another name", null, null);
		Course other = Course.create(CourseId.generate(), "Golfclub Sonnenhof", null, null, HoleCount.EIGHTEEN,
				List.of(YELLOW), CREATOR, CREATED);

		assertThat(course).isEqualTo(renamed)
			.hasSameHashCodeAs(renamed)
			.isNotEqualTo(other)
			.doesNotHaveSameHashCodeAs(other)
			.isNotEqualTo("course");
		assertThat(course).isEqualTo(course);
	}

	@Test
	void describesItselfByIdAndName() {
		assertThat(course(List.of(YELLOW))).hasToString("Course[id=" + ID.value() + ", name=Golfclub Sonnenhof]");
	}

}
