package com.prokxi.dimples.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import com.prokxi.dimples.service.GolferUserDetails;
import com.prokxi.dimples.service.GolferUserDetailsService;
import com.prokxi.dimples.service.LoginThrottle;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Does what Spring Security's form login and logout filters would do, for the JSON
 * endpoints in {@link AuthController}: authenticate, protect against session fixation,
 * store the security context in the session and issue or delete the remember-me token.
 */
@Component
@RequiredArgsConstructor
class SessionLogin {

	private final AuthenticationManager authenticationManager;

	private final GolferUserDetailsService userDetailsService;

	private final LoginThrottle loginThrottle;

	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

	private final SecurityContextRepository securityContextRepository;

	private final RequestAttributeRememberMeServices rememberMeServices;

	private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
		.getContextHolderStrategy();

	/**
	 * Checks the credentials and logs the golfer in.
	 * @throws AuthenticationException for a wrong email or password (answered with 401)
	 * @throws com.prokxi.dimples.exception.TooManyRequestsException if the email is
	 * blocked after too many failures
	 */
	GolferUserDetails login(String email, String password, boolean rememberMe, HttpServletRequest request,
			HttpServletResponse response) {
		this.loginThrottle.checkAllowed(email);
		Authentication authentication;
		try {
			authentication = this.authenticationManager
				.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
		}
		catch (AuthenticationException ex) {
			this.loginThrottle.recordFailure(email);
			throw ex;
		}
		this.loginThrottle.recordSuccess(email);
		establish(authentication, rememberMe, request, response);
		return (GolferUserDetails) authentication.getPrincipal();
	}

	/**
	 * Logs in a golfer who has just registered, without checking the password again.
	 */
	void loginRegistered(String email, HttpServletRequest request, HttpServletResponse response) {
		GolferUserDetails user = this.userDetailsService.loadUserByUsername(email);
		user.eraseCredentials();
		establish(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()), false, request,
				response);
	}

	/**
	 * Deletes the remember-me token, invalidates the session and clears its cookie.
	 */
	void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = this.securityContextHolderStrategy.getContext().getAuthentication();
		this.rememberMeServices.logout(request, response, authentication);
		SecurityContextLogoutHandler contextLogoutHandler = new SecurityContextLogoutHandler();
		contextLogoutHandler.setSecurityContextRepository(this.securityContextRepository);
		contextLogoutHandler.logout(request, response, authentication);
		new CookieClearingLogoutHandler("JSESSIONID").logout(request, response, authentication);
	}

	private void establish(Authentication authentication, boolean rememberMe, HttpServletRequest request,
			HttpServletResponse response) {
		this.sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
		SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		this.securityContextHolderStrategy.setContext(context);
		this.securityContextRepository.saveContext(context, request, response);
		if (rememberMe) {
			request.setAttribute(RequestAttributeRememberMeServices.REMEMBER_ME_ATTRIBUTE, true);
			this.rememberMeServices.loginSuccess(request, response, authentication);
		}
	}

}
