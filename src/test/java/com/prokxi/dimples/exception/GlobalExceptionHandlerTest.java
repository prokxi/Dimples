package com.prokxi.dimples.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ThrowingController.class)
@Import(GlobalExceptionHandlerTest.ThrowingController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

	private static final String SECRET = "max.mustermann@example.com";

	@Autowired
	private MockMvcTester mvc;

	@Test
	void invalidRequestBodyReturns400WithInvalidFields() {
		MvcTestResult result = mvc.post()
			.uri("/test/body")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"name": "", "password": "%s"}
					""".formatted(SECRET))
			.exchange();

		assertProblem(result, HttpStatus.BAD_REQUEST);
		assertThat(result).bodyJson().extractingPath("$.errors[*].field").asArray().containsExactly("name", "password");
		assertThat(result).bodyJson().extractingPath("$.errors[0].message").asString().isEqualTo("must not be blank");
		assertThat(result).body().asString().doesNotContain(SECRET);
	}

	@Test
	void unreadableRequestBodyReturns400() {
		MvcTestResult result = mvc.post()
			.uri("/test/body")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{not json")
			.exchange();

		assertProblem(result, HttpStatus.BAD_REQUEST);
	}

	@Test
	void notFoundExceptionReturns404() {
		MvcTestResult result = mvc.get().uri("/test/not-found").exchange();

		assertProblem(result, HttpStatus.NOT_FOUND);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Round not found.");
	}

	@Test
	void accessDeniedExceptionReturns403() {
		MvcTestResult result = mvc.get().uri("/test/access-denied").exchange();

		assertProblem(result, HttpStatus.FORBIDDEN);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Only organisers can do this.");
	}

	@Test
	void conflictExceptionReturns409() {
		MvcTestResult result = mvc.get().uri("/test/conflict").exchange();

		assertProblem(result, HttpStatus.CONFLICT);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Email address is already in use.");
	}

	@Test
	void unexpectedExceptionReturns500WithoutInternals() {
		MvcTestResult result = mvc.get().uri("/test/unexpected").exchange();

		assertProblem(result, HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("An unexpected error occurred.");
		assertThat(result).body()
			.asString()
			.doesNotContain(SECRET)
			.doesNotContain("IllegalStateException")
			.doesNotContain("ThrowingController");
	}

	@Test
	void springSecurityAccessDeniedIsLeftToTheSecurityFilterChain() {
		MvcTestResult result = mvc.get().uri("/test/security-denied").exchange();

		assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void unknownPathReturns404() {
		MvcTestResult result = mvc.get().uri("/test/does-not-exist").exchange();

		assertProblem(result, HttpStatus.NOT_FOUND);
	}

	private static void assertProblem(MvcTestResult result, HttpStatus status) {
		assertThat(result).hasStatus(status).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(status.value());
		assertThat(result).bodyJson().extractingPath("$.title").isEqualTo(status.getReasonPhrase());
	}

	record TestBody(@NotBlank String name, @Size(min = 100) String password) {
	}

	@RestController
	static class ThrowingController {

		@PostMapping("/test/body")
		void body(@Valid @RequestBody TestBody body) {
		}

		@GetMapping("/test/not-found")
		void notFound() {
			throw new NotFoundException("Round not found.");
		}

		@GetMapping("/test/access-denied")
		void accessDenied() {
			throw new AccessDeniedException("Only organisers can do this.");
		}

		@GetMapping("/test/conflict")
		void conflict() {
			throw new ConflictException("Email address is already in use.");
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("Broken for " + SECRET);
		}

		@GetMapping("/test/security-denied")
		void securityDenied() {
			throw new org.springframework.security.access.AccessDeniedException("Denied by Spring Security");
		}

	}

}
