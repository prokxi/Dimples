package com.prokxi.dimples.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The logged-in golfer as Spring Security sees it. The username is the email address
 * (used for login and remember-me tokens); controllers use {@link #golferId()} to call
 * services.
 */
public final class GolferUserDetails implements UserDetails, CredentialsContainer {

	private final UUID golferId;

	private final String email;

	private String passwordHash;

	private final List<GrantedAuthority> authorities;

	GolferUserDetails(Golfer golfer) {
		this.golferId = golfer.id().value();
		this.email = golfer.email().orElseThrow().value();
		this.passwordHash = golfer.passwordHash().orElseThrow();
		this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + golfer.role().name()));
	}

	public GolferId golferId() {
		return new GolferId(this.golferId);
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public String getPassword() {
		return this.passwordHash;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public void eraseCredentials() {
		this.passwordHash = null;
	}

	@Override
	public String toString() {
		return "GolferUserDetails[golferId=" + this.golferId + "]";
	}

}
