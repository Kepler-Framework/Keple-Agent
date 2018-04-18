package com.kepler.connection;

/**
 * 响应状态
 * 
 * @author KimShen
 *
 */
public enum ResponseStatus {

	SUCCESS(0), FAILED(1);

	private int code;

	private ResponseStatus(int code) {
		this.code = code;
	}

	public int code() {
		return this.code;
	}
}
