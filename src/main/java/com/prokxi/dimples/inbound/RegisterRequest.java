package com.prokxi.dimples.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for a new account")
public record RegisterRequest(
		@Schema(description = "Login name; trimmed and lower-cased", example = "max@example.com",
				maxLength = 254) @NotNull String email,
		@Schema(description = "10 to 64 characters and at most 72 bytes in UTF-8", minLength = 10,
				maxLength = 64) @NotNull String password,
		@Schema(description = "Name shown to other participants; 2 to 40 characters", example = "Max", minLength = 2,
				maxLength = 40) @NotNull String displayName) {

	@Override
	public String toString() {
		return "RegisterRequest[***]";
	}

}
