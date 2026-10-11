package com.prokxi.dimples.domain;

public enum GolferStatus {

	ACTIVE,

	/**
	 * The account was deleted and its personal data removed. The golfer stays so that
	 * past results remain consistent.
	 */
	DELETED

}
