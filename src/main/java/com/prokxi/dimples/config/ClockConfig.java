package com.prokxi.dimples.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfig {

	/**
	 * The system clock in the server's time zone, which decides what "today" is (e.g. for
	 * the current Handicap Index).
	 */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

}
