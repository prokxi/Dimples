package com.prokxi.dimples.inbound;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Persistent-token remember-me for JSON logins: the login request body says whether to
 * remember the golfer, so {@link SessionLogin} marks the request with an attribute
 * instead of the form parameter Spring Security looks for.
 */
public class RequestAttributeRememberMeServices extends PersistentTokenBasedRememberMeServices {

	static final String REMEMBER_ME_ATTRIBUTE = RequestAttributeRememberMeServices.class.getName() + ".REMEMBER_ME";

	public RequestAttributeRememberMeServices(String key, UserDetailsService userDetailsService,
			PersistentTokenRepository tokenRepository) {
		super(key, userDetailsService, tokenRepository);
	}

	@Override
	protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
		return Boolean.TRUE.equals(request.getAttribute(REMEMBER_ME_ATTRIBUTE));
	}

}
