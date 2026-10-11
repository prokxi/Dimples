package com.prokxi.dimples.config;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import com.prokxi.dimples.inbound.RequestAttributeRememberMeServices;
import com.prokxi.dimples.inbound.SecurityProblemHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Session login for the SPA (SPEC-1, sections 4.2, 4.3 and 5). Login, registration and
 * logout are JSON endpoints in {@code AuthController}; there is no form or basic login.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	private static final Duration REMEMBER_ME_VALIDITY = Duration.ofDays(30);

	/**
	 * Only signs the in-memory remember-me authentication. The tokens themselves are
	 * random values stored in the database, so a new key after a restart does not log
	 * anyone out.
	 */
	private final String rememberMeKey = UUID.randomUUID().toString();

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProblemHandler problemHandler,
			RequestAttributeRememberMeServices rememberMeServices,
			SecurityContextRepository securityContextRepository) {
		CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfTokenRepository.setCookieCustomizer((cookie) -> cookie.sameSite("Lax"));
		http.authorizeHttpRequests((requests) -> requests.requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf")
			.permitAll()
			.requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
			.permitAll()
			.anyRequest()
			.authenticated())
			.csrf((csrf) -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
			.securityContext((context) -> context.securityContextRepository(securityContextRepository))
			.rememberMe((rememberMe) -> rememberMe.rememberMeServices(rememberMeServices).key(this.rememberMeKey))
			.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(problemHandler)
				.accessDeniedHandler(problemHandler))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
				new HttpSessionSecurityContextRepository());
	}

	/**
	 * The SPA reads the {@code XSRF-TOKEN} cookie and sends it back in the
	 * {@code X-XSRF-TOKEN} header.
	 */
	@Bean
	CsrfTokenRepository csrfTokenRepository() {
		CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		repository.setCookieCustomizer((cookie) -> cookie.sameSite("Lax"));
		return repository;
	}

	/**
	 * What form login would do on success: a new session ID against session fixation and
	 * a new CSRF token.
	 */
	@Bean
	SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
		return new CompositeSessionAuthenticationStrategy(List.of(new ChangeSessionIdAuthenticationStrategy(),
				new CsrfAuthenticationStrategy(csrfTokenRepository)));
	}

	@Bean
	RequestAttributeRememberMeServices rememberMeServices(UserDetailsService userDetailsService,
			DataSource dataSource) {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		RequestAttributeRememberMeServices services = new RequestAttributeRememberMeServices(this.rememberMeKey,
				userDetailsService, tokenRepository);
		services.setTokenValiditySeconds((int) REMEMBER_ME_VALIDITY.toSeconds());
		services.setUseSecureCookie(true);
		return services;
	}

}
