package com.prokxi.dimples.inbound;

import java.util.UUID;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.prokxi.dimples.TestcontainersConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flows of SPEC-1 sections 4.1 to 4.3 against the real security configuration
 * and PostgreSQL. The application context, and with it the login throttle, is shared, so
 * every test uses its own email address.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class AuthenticationFlowTest {

	private static final String PASSWORD = "correct horse battery";

	/**
	 * Any authenticated endpoint works as a probe; the OpenAPI spec needs a login.
	 */
	private static final String PROTECTED = "/v3/api-docs";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcClient jdbc;

	private static String uniqueEmail() {
		return "golfer-" + UUID.randomUUID() + "@example.com";
	}

	private static String registerJson(String email, String password, String displayName) {
		return """
				{"email": "%s", "password": "%s", "displayName": "%s"}
				""".formatted(email, password, displayName);
	}

	private static String loginJson(String email, String password, boolean rememberMe) {
		return """
				{"email": "%s", "password": "%s", "rememberMe": %s}
				""".formatted(email, password, rememberMe);
	}

	private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
		return request.contentType(MediaType.APPLICATION_JSON).content(body);
	}

	/**
	 * Gets a CSRF token like the SPA does and sends it back as cookie and header. The
	 * {@code csrf()} helper of spring-security-test is not used: it replaces the token
	 * repository of the shared context with a session-based one.
	 */
	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
		Cookie token = this.mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
		return request.cookie(token).header("X-XSRF-TOKEN", token.getValue());
	}

	private MvcResult register(String email) throws Exception {
		return this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson(email, PASSWORD, "Max"))))
			.andExpect(status().isCreated())
			.andReturn();
	}

	private MvcResult login(String email, String password, boolean rememberMe) throws Exception {
		return this.mvc.perform(withCsrf(json(post("/api/v1/auth/login"), loginJson(email, password, rememberMe))))
			.andReturn();
	}

	private static MockHttpSession session(MvcResult result) {
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private int rememberMeTokens(String email) {
		return this.jdbc.sql("select count(*) from persistent_logins where username = ?")
			.param(email)
			.query(Integer.class)
			.single();
	}

	@Test
	void registrationCreatesTheGolferStoresABcryptHashAndLogsIn() throws Exception {
		String email = uniqueEmail();

		MvcResult result = this.mvc
			.perform(withCsrf(json(post("/api/v1/auth/register"),
					registerJson(" " + email.toUpperCase() + " ", PASSWORD, " Max Muster "))))
			.andExpect(status().isCreated())
			.andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/golfers/me"))
			.andExpect(jsonPath("$.id").isString())
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.displayName").value("Max Muster"))
			.andExpect(jsonPath("$.distanceUnit").value("METERS"))
			.andExpect(jsonPath("$.currentHandicapIndex").isEmpty())
			.andExpect(jsonPath("$.mustChangePassword").value(false))
			.andReturn();

		String hash = this.jdbc.sql("select password_hash from golfer where email = ?")
			.param(email)
			.query(String.class)
			.single();
		assertThat(hash).startsWith("{bcrypt}").doesNotContain(PASSWORD);
		this.mvc.perform(get(PROTECTED).session(session(result))).andExpect(status().isOk());
	}

	@Test
	void registrationWithTheEmailOfAnActiveGolferReturns409() throws Exception {
		String email = uniqueEmail();
		register(email);

		this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson(email, PASSWORD, "Other"))))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("Email address is already in use."));
	}

	@Test
	void registrationRejectsPasswordsOutsideTheLimits() throws Exception {
		for (String password : new String[] { "a".repeat(9), "a".repeat(65), "ä".repeat(37) }) {
			this.mvc
				.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson(uniqueEmail(), password, "Max"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("password"));
		}
	}

	@Test
	void registrationRejectsInvalidAndMissingFields() throws Exception {
		this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson("not-an-email", PASSWORD, "Max"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("email"));
		this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson(uniqueEmail(), PASSWORD, " M "))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("displayName"));
		this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), "{}")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.length()").value(3));
	}

	@Test
	void loginStartsASession() throws Exception {
		String email = uniqueEmail();
		register(email);

		MvcResult result = this.mvc
			.perform(withCsrf(json(post("/api/v1/auth/login"), loginJson(email.toUpperCase(), PASSWORD, false))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(cookie().doesNotExist("remember-me"))
			.andReturn();

		this.mvc.perform(get(PROTECTED).session(session(result))).andExpect(status().isOk());
	}

	@Test
	void wrongPasswordAndUnknownEmailGetTheSame401() throws Exception {
		String email = uniqueEmail();
		register(email);

		MvcResult wrongPassword = login(email, "wrong password", false);
		MvcResult unknownEmail = login(uniqueEmail(), PASSWORD, false);

		assertThat(wrongPassword.getResponse().getStatus()).isEqualTo(401);
		assertThat(unknownEmail.getResponse().getStatus()).isEqualTo(401);
		assertThat(wrongPassword.getResponse().getContentAsString())
			.isEqualTo(unknownEmail.getResponse().getContentAsString());
		assertThat(wrongPassword.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	@Test
	void sixthFailedLoginWithinFifteenMinutesReturns429() throws Exception {
		String email = uniqueEmail();
		register(email);
		for (int i = 0; i < 5; i++) {
			assertThat(login(email, "wrong password", false).getResponse().getStatus()).isEqualTo(401);
		}

		MvcResult sixth = login(email, PASSWORD, false);

		assertThat(sixth.getResponse().getStatus()).isEqualTo(429);
		assertThat(sixth.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	@Test
	void rememberMeKeepsTheGolferLoggedInWithoutASession() throws Exception {
		String email = uniqueEmail();
		register(email);

		MvcResult result = login(email, PASSWORD, true);
		Cookie rememberMe = result.getResponse().getCookie("remember-me");

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		assertThat(rememberMe).isNotNull();
		assertThat(rememberMe.getMaxAge()).isEqualTo(30 * 24 * 60 * 60);
		assertThat(rememberMe.isHttpOnly()).isTrue();
		assertThat(rememberMe.getSecure()).isTrue();
		assertThat(rememberMeTokens(email)).isOne();
		// A request without the session, as after session expiry or an app restart.
		this.mvc.perform(get(PROTECTED).cookie(rememberMe)).andExpect(status().isOk());
	}

	@Test
	void changingRequestsWithoutCsrfTokenAreRejected() throws Exception {
		this.mvc.perform(json(post("/api/v1/auth/login"), loginJson(uniqueEmail(), PASSWORD, false)))
			.andExpect(status().isForbidden())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void csrfEndpointIssuesACookieTheSpaSendsBackInTheHeader() throws Exception {
		String email = uniqueEmail();
		register(email);

		MvcResult csrfResult = this.mvc.perform(get("/api/v1/auth/csrf"))
			.andExpect(status().isNoContent())
			.andExpect(cookie().exists("XSRF-TOKEN"))
			.andExpect(cookie().httpOnly("XSRF-TOKEN", false))
			.andReturn();
		Cookie token = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		this.mvc
			.perform(json(post("/api/v1/auth/login"), loginJson(email, PASSWORD, false)).cookie(token)
				.header("X-XSRF-TOKEN", token.getValue()))
			.andExpect(status().isOk())
			// The token is rotated on login; the SPA fetches a new one.
			.andExpect(cookie().maxAge("XSRF-TOKEN", 0));
		this.mvc
			.perform(json(post("/api/v1/auth/login"), loginJson(email, PASSWORD, false)).cookie(token)
				.header("X-XSRF-TOKEN", "forged"))
			.andExpect(status().isForbidden());
	}

	@Test
	void unauthenticatedRequestsGet401InsteadOfARedirect() throws Exception {
		this.mvc.perform(get(PROTECTED))
			.andExpect(status().isUnauthorized())
			.andExpect(header().doesNotExist(HttpHeaders.LOCATION))
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(401));
		this.mvc.perform(withCsrf(post("/api/v1/auth/logout"))).andExpect(status().isUnauthorized());
	}

	@Test
	void logoutEndsTheSessionAndTheRememberMeToken() throws Exception {
		String email = uniqueEmail();
		register(email);
		MvcResult result = login(email, PASSWORD, true);
		MockHttpSession session = session(result);
		Cookie rememberMe = result.getResponse().getCookie("remember-me");

		this.mvc.perform(withCsrf(post("/api/v1/auth/logout").session(session).cookie(rememberMe)))
			.andExpect(status().isNoContent())
			.andExpect(cookie().maxAge("remember-me", 0))
			.andExpect(cookie().maxAge("JSESSIONID", 0));

		assertThat(session.isInvalid()).isTrue();
		assertThat(rememberMeTokens(email)).isZero();
		this.mvc.perform(get(PROTECTED).cookie(rememberMe)).andExpect(status().isUnauthorized());
	}

	@Test
	void logsContainNoPasswordsHashesOrEmails(CapturedOutput output) throws Exception {
		String email = uniqueEmail();
		register(email);
		login(email, "wrong password", false);
		login(email, PASSWORD, true);
		this.mvc.perform(withCsrf(json(post("/api/v1/auth/register"), registerJson(email, PASSWORD, "Max"))));

		String hash = this.jdbc.sql("select password_hash from golfer where email = ?")
			.param(email)
			.query(String.class)
			.single();
		assertThat(output.getAll()).doesNotContain(email)
			.doesNotContain(PASSWORD)
			.doesNotContain("wrong password")
			.doesNotContain(hash);
	}

	@Test
	@WithMockUser
	void authEndpointsAreInTheOpenApiSpec() throws Exception {
		this.mvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/api/v1/auth/csrf'].get").exists())
			.andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.responses['201']").exists())
			.andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.responses['409']").exists())
			.andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['429']").exists())
			.andExpect(jsonPath("$.paths['/api/v1/auth/logout'].post.responses['204']").exists())
			.andExpect(jsonPath("$.components.schemas.RegisterRequest").exists())
			.andExpect(jsonPath("$.components.schemas.LoginRequest").exists())
			.andExpect(jsonPath("$.components.schemas.GolferResponse").exists());
	}

}
