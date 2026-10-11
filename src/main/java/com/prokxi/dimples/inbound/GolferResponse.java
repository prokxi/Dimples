package com.prokxi.dimples.inbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.HandicapIndex;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The logged-in golfer's own profile")
public record GolferResponse(UUID id, String email, String displayName, @Schema(allowableValues = {
		"METERS", "YARDS" }) String distanceUnit,
		@Schema(description = "Plus handicaps are negative (+2.3 is -2.3); null if none is recorded yet",
				example = "18.4", nullable = true) BigDecimal currentHandicapIndex,
		@Schema(description = "Set after a password reset by the operator") boolean mustChangePassword){

	static GolferResponse from(Golfer golfer, LocalDate today) {
		return new GolferResponse(golfer.id().value(), golfer.email().orElseThrow().value(),
				golfer.displayName().orElseThrow().value(), golfer.distanceUnit().name(),
				golfer.currentHandicapIndex(today).map(HandicapIndex::value).orElse(null), golfer.mustChangePassword());
	}

}
