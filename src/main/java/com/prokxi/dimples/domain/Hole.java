package com.prokxi.dimples.domain;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A hole of a tee. {@link Tee} checks that the numbers and stroke indexes of its holes
 * fit together.
 */
public final class Hole {

	public static final int MIN_PAR = 3;

	public static final int MAX_PAR = 6;

	public static final int MAX_NUMBER = 18;

	public static final int MAX_LENGTH_METERS = 800;

	private final int number;

	private final int par;

	private final int strokeIndex;

	private final Integer lengthMeters;

	/**
	 * @param lengthMeters {@code null} if the length is unknown
	 */
	public Hole(int number, int par, int strokeIndex, Integer lengthMeters) {
		if (number < 1 || number > MAX_NUMBER) {
			throw new IllegalArgumentException("Hole number must be between 1 and " + MAX_NUMBER);
		}
		if (par < MIN_PAR || par > MAX_PAR) {
			throw new IllegalArgumentException("Par must be between " + MIN_PAR + " and " + MAX_PAR);
		}
		if (strokeIndex < 1 || strokeIndex > MAX_NUMBER) {
			throw new IllegalArgumentException("Stroke index must be between 1 and " + MAX_NUMBER);
		}
		if (lengthMeters != null && (lengthMeters < 1 || lengthMeters > MAX_LENGTH_METERS)) {
			throw new IllegalArgumentException("Hole length must be between 1 and " + MAX_LENGTH_METERS + " meters");
		}
		this.number = number;
		this.par = par;
		this.strokeIndex = strokeIndex;
		this.lengthMeters = lengthMeters;
	}

	public int number() {
		return this.number;
	}

	public int par() {
		return this.par;
	}

	public int strokeIndex() {
		return this.strokeIndex;
	}

	public OptionalInt lengthMeters() {
		return (this.lengthMeters != null) ? OptionalInt.of(this.lengthMeters) : OptionalInt.empty();
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof Hole hole && this.number == hole.number && this.par == hole.par
				&& this.strokeIndex == hole.strokeIndex && Objects.equals(this.lengthMeters, hole.lengthMeters));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.number, this.par, this.strokeIndex, this.lengthMeters);
	}

	@Override
	public String toString() {
		return "Hole[number=" + this.number + ", par=" + this.par + ", strokeIndex=" + this.strokeIndex
				+ ", lengthMeters=" + this.lengthMeters + "]";
	}

}
