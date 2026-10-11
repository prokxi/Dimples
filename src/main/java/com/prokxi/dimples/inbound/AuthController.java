package com.prokxi.dimples.inbound;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.Password;
import com.prokxi.dimples.service.AccountService;
import com.prokxi.dimples.service.GolferUserDetails;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Registration, session login and logout")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AccountService accountService;

	private final SessionLogin sessionLogin;

	private final Clock clock;

	@Operation(summary = "Get a CSRF token",
			description = "Sets the XSRF-TOKEN cookie. Send its value in the X-XSRF-TOKEN header on every changing request.")
	@ApiResponse(responseCode = "204", description = "Cookie set")
	@GetMapping("/csrf")
	public ResponseEntity<Void> csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
		// Reading the deferred token makes Spring Security write the cookie.
		csrfToken.getToken();
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Register a new golfer", description = "Creates the account and logs the golfer in.")
	@ApiResponse(responseCode = "201", description = "Registered and logged in")
	@ApiResponse(responseCode = "400", description = "Invalid fields")
	@ApiResponse(responseCode = "409", description = "Email address is already in use")
	@PostMapping("/register")
	public ResponseEntity<GolferResponse> register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		Golfer golfer = this.accountService.register(RequestFields.parse("email", () -> new EmailAddress(body.email())),
				RequestFields.parse("password", () -> new Password(body.password())),
				RequestFields.parse("displayName", () -> new DisplayName(body.displayName())));
		this.sessionLogin.loginRegistered(golfer.email().orElseThrow().value(), request, response);
		return ResponseEntity.created(URI.create("/api/v1/golfers/me")).body(GolferResponse.from(golfer, today()));
	}

	@Operation(summary = "Log in", description = "Starts a session. With rememberMe, also sets a remember-me cookie.")
	@ApiResponse(responseCode = "200", description = "Logged in")
	@ApiResponse(responseCode = "400", description = "Invalid fields")
	@ApiResponse(responseCode = "401", description = "Wrong email or password")
	@ApiResponse(responseCode = "429", description = "Too many failed logins for this email; try again later")
	@PostMapping("/login")
	public GolferResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		GolferUserDetails user = this.sessionLogin.login(body.email(), body.password(), body.rememberMe(), request,
				response);
		return GolferResponse.from(this.accountService.getGolfer(user.golferId()), today());
	}

	@Operation(summary = "Log out", description = "Ends the session and deletes the remember-me token.")
	@ApiResponse(responseCode = "204", description = "Logged out")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		this.sessionLogin.logout(request, response);
		return ResponseEntity.noContent().build();
	}

	private LocalDate today() {
		return LocalDate.now(this.clock);
	}

}
