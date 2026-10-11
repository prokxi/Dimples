package com.prokxi.dimples.service;

import com.prokxi.dimples.domain.EmailAddress;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads active golfers by email for login and remember-me. Deleted golfers cannot log in;
 * Spring Security answers them like a wrong password.
 */
@Service
@RequiredArgsConstructor
public class GolferUserDetailsService implements UserDetailsService {

	private final GolferRepository golferRepository;

	@Override
	@Transactional(readOnly = true)
	public GolferUserDetails loadUserByUsername(String email) {
		EmailAddress address;
		try {
			address = new EmailAddress(email);
		}
		catch (IllegalArgumentException ex) {
			throw new UsernameNotFoundException("Unknown golfer");
		}
		return this.golferRepository.findActiveByEmail(address)
			.map(GolferUserDetails::new)
			.orElseThrow(() -> new UsernameNotFoundException("Unknown golfer"));
	}

}
