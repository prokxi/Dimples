package com.prokxi.dimples.domain;

/**
 * Installation-wide role. Organisers are a role per series, not a golfer role.
 */
public enum GolferRole {

	GOLFER,

	/**
	 * Runs the installation: resets passwords and manages all courses.
	 */
	OPERATOR

}
