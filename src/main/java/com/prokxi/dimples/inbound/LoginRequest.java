package com.prokxi.dimples.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@Schema(example = "max@example.com") @NotBlank String email, @NotBlank String password,
		@Schema(description = "Stay logged in for 30 days, also after the session has expired") boolean rememberMe) {

	@Override
	public String toString() {
		return "LoginRequest[***]";
	}

}
