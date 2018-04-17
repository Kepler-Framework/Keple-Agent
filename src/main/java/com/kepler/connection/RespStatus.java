package com.kepler.connection;

/**
 * @author KimShen
 *
 */
public enum RespStatus {

	SUCCESS(0), FAILED(1);

	private int code;

	private RespStatus(int code) {
		this.code = code;
	}

	public int code() {
		return this.code;
	}
}
